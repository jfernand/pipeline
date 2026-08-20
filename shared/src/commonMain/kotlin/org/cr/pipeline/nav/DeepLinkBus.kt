/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.nav

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Pushes deep-link URIs (e.g. `"pipeline://app/8"`) into the running UI's nav graph from outside
 * the composition. The MCP server is the only current producer — it runs as a plain http4k
 * `HttpHandler` with no `NavHostController` of its own, so this is how a tool call like
 * `open_application` reaches the actual window.
 */
interface DeepLinkBus {
    val deepLinks: SharedFlow<String>
    suspend fun navigate(deepLink: String)
}

class DefaultDeepLinkBus : DeepLinkBus {
    private val _deepLinks = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val deepLinks: SharedFlow<String> = _deepLinks.asSharedFlow()

    override suspend fun navigate(deepLink: String) {
        _deepLinks.emit(deepLink)
    }
}
