/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.nav.DeepLinkBus

actual fun createMcpServerController(
    repository: JobApplicationRepository,
    preferencesStore: PreferencesStore,
    deepLinkBus: DeepLinkBus,
    logger: Logger,
): McpServerController = UnsupportedMcpServerController
