package org.cr.pipeline.data.mcp

import org.cr.pipeline.data.JobApplicationRepository

actual fun createMcpServerController(repository: JobApplicationRepository): McpServerController = UnsupportedMcpServerController
