/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import co.touchlab.kermit.Logger
import org.cr.pipeline.data.mcp.McpServerController
import org.cr.pipeline.data.mcp.createMcpServerController
import org.cr.pipeline.nav.DefaultDeepLinkBus
import org.cr.pipeline.nav.DeepLinkBus
import org.koin.dsl.module

/** A single commonMain module (unlike [platformDataModule]) since [createMcpServerController] is
 *  itself an expect/actual factory function — no per-platform Koin wiring needed here. [DeepLinkBus]
 *  lives here too: it's plain Kotlin with no platform dependency, and its only current producer is
 *  the MCP server's `open_application` tool. */
val mcpDataModule = module {
    single<DeepLinkBus> { DefaultDeepLinkBus() }
    single<McpServerController> { createMcpServerController(get(), get(), get(), get<Logger>().withTag("McpServer")) }
}
