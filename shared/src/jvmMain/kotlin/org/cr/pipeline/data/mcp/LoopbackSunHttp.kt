package org.cr.pipeline.data.mcp

import com.sun.net.httpserver.HttpServer
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kServer
import org.http4k.server.HttpExchangeHandler
import org.http4k.server.ServerConfig
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors.newWorkStealingPool

/**
 * http4k's stock `SunHttp` binds `InetSocketAddress(port)` — every interface, not just loopback.
 * That's wrong for this server: it has no auth and can create/edit applications, so it must only
 * ever be reachable from this machine. http4k's own `SunHttp` doc comment invites exactly this —
 * duplicating its (internal, so not reusable directly) implementation with the one line changed.
 */
class LoopbackSunHttp(private val port: Int = 0) : ServerConfig {
    override fun toServer(http: HttpHandler): Http4kServer {
        val executor = newWorkStealingPool()
        val server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1000)
        return object : Http4kServer {
            override fun port(): Int = if (port > 0) port else server.address.port

            override fun start(): Http4kServer = apply {
                server.createContext("/", HttpExchangeHandler(http))
                server.executor = executor
                server.start()
            }

            override fun stop(): Http4kServer = apply { server.stop(0) }
        }
    }
}
