/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.tablet.TabletSyncContent

/** Phone entry point for the same Sync content the tablet NavRail's "Sync" tab opens —
 *  TabletSyncContent already adapts its own layout below the phone-width breakpoint. */
@Composable
fun SyncScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Column(modifier.fillMaxSize()) {
        PlTopBar(title = "Sync", leftIcon = Icons.AutoMirrored.Filled.ArrowBack, onLeftClick = onBack)
        TabletSyncContent(modifier = Modifier.weight(1f))
    }
}
