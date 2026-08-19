package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore

actual fun createMcpServerController(
    repository: JobApplicationRepository,
    preferencesStore: PreferencesStore,
    logger: Logger,
): McpServerController = UnsupportedMcpServerController
