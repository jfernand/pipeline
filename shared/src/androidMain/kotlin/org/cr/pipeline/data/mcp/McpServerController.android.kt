package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import org.cr.pipeline.data.JobApplicationRepository

actual fun createMcpServerController(
    repository: JobApplicationRepository,
    logger: Logger,
): McpServerController = UnsupportedMcpServerController
