/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption.SO_BACKLOG
import io.netty.channel.ChannelOption.SO_KEEPALIVE
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kChannelInitializer
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.MILLISECONDS

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
    // http4k's own `MAX_REQUEST_SIZE` default is `internal`, so it isn't visible across the
    // module boundary — this mirrors its value (10MB) rather than reimplementing request sizing.
    private val maxRequestSize = 10 * 1024 * 1024

    override fun toServer(http: HttpHandler): Http4kServer = object : Http4kServer {
        private val masterGroup = MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory())
        private val workerGroup = MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory())
        private val childHandler = Http4kChannelInitializer(null, http, maxRequestSize)

        private var closeFuture: ChannelFuture? = null
        private lateinit var address: InetSocketAddress

        override fun start(): Http4kServer = apply {
            val bootstrap = ServerBootstrap()
            bootstrap.group(masterGroup, workerGroup)
                .channelFactory { NioServerSocketChannel() }
                .childHandler(childHandler)
                .option(SO_BACKLOG, 1000)
                .childOption(SO_KEEPALIVE, true)

            val channel = bootstrap.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port)).sync().channel()
            address = channel.localAddress() as InetSocketAddress
            closeFuture = channel.closeFuture()
        }

        override fun stop() = apply {
            closeFuture?.cancel(false)
            childHandler.close()
            workerGroup.shutdownGracefully(0, 2000, MILLISECONDS).sync()
            masterGroup.shutdownGracefully(0, 2000, MILLISECONDS).sync()
        }

        override fun port(): Int = if (port > 0) port else address.port
    }
}
