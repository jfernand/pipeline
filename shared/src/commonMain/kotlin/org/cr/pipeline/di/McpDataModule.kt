package org.cr.pipeline.di

import org.cr.pipeline.data.mcp.McpServerController
import org.cr.pipeline.data.mcp.createMcpServerController
import org.koin.dsl.module

/** A single commonMain module (unlike [platformDataModule]) since [createMcpServerController] is
 *  itself an expect/actual factory function — no per-platform Koin wiring needed here. */
val mcpDataModule = module {
    single<McpServerController> { createMcpServerController(get()) }
}
