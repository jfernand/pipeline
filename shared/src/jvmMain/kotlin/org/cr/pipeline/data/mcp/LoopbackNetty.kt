package org.cr.pipeline.data.mcp

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpServerKeepAliveHandler
import io.netty.handler.stream.ChunkedWriteHandler
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kChannelHandler
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * http4k's stock `Netty` server config binds `bootstrap.bind(port)` — every interface, not just
 * loopback, the same problem [LoopbackSunHttp] used to work around for the JDK-backed engine.
 * `Netty` itself isn't reusable directly (its bind address isn't configurable), so this
 * duplicates it with the one line changed, same as before.
 *
 * The JDK's built-in `com.sun.net.httpserver.HttpServer` (what `LoopbackSunHttp` wrapped) is
 * documented by http4k as unfit for real client traffic — it has known HTTP/1.1 keep-alive
 * handling problems under clients that pool/reuse/retry connections. That's exactly what caused
 * MCP clients doing a normal connection handshake to hang and time out against this server, even
 * though one-shot requests (curl, a fresh `HttpURLConnection`) always got fast, correct replies.
 * Netty doesn't have that problem.
 */
class LoopbackNetty(private val port: Int = 0) : ServerConfig {
    override fun toServer(http: HttpHandler): Http4kServer = object : Http4kServer {
        private val masterGroup = MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory())
        private val workerGroup = MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory())

        private var closeFuture: ChannelFuture? = null
        private lateinit var address: InetSocketAddress

        override fun start(): Http4kServer = apply {
            val bootstrap = ServerBootstrap()
            bootstrap.group(masterGroup, workerGroup)
                .channelFactory { NioServerSocketChannel() }
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast("codec", HttpServerCodec())
                        ch.pipeline().addLast("keepAlive", HttpServerKeepAliveHandler())
                        ch.pipeline().addLast("aggregator", HttpObjectAggregator(Int.MAX_VALUE))
                        ch.pipeline().addLast("streamer", ChunkedWriteHandler())
                        ch.pipeline().addLast("httpHandler", Http4kChannelHandler(http))
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1000)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            val channel = bootstrap.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port)).sync().channel()
            address = channel.localAddress() as InetSocketAddress
            closeFuture = channel.closeFuture()
        }

        override fun stop() = apply {
            closeFuture?.cancel(false)
            workerGroup.shutdownGracefully(0, 2000, java.util.concurrent.TimeUnit.MILLISECONDS).sync()
            masterGroup.shutdownGracefully(0, 2000, java.util.concurrent.TimeUnit.MILLISECONDS).sync()
        }

        override fun port(): Int = if (port > 0) port else address.port
    }
}
