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
import org.cr.pipeline.ui.tablet.DevToolsContent

/** Phone entry point for the same Dev Tools content the tablet NavRail's "Dev Tools" tab opens —
 *  DevToolsContent is already a plain scrollable column with no tablet-width assumptions. */
@Composable
fun DevToolsScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Column(modifier.fillMaxSize()) {
        PlTopBar(title = "Dev Tools", leftIcon = Icons.AutoMirrored.Filled.ArrowBack, onLeftClick = onBack)
        DevToolsContent(modifier = Modifier.weight(1f))
    }
}
