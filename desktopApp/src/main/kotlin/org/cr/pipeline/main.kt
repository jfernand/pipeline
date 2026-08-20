/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

private const val DEEP_LINK_SCHEME = "pipeline://"

fun main(args: Array<String>) = application {
    // Scanned by prefix, not position: the OS (or a wrapper script) can prepend its own
    // arguments before the actual URI (e.g. macOS Launch Services injects a -psn_... flag),
    // so args[0] is not reliably the link.
    val deepLink = args.firstOrNull { it.startsWith(DEEP_LINK_SCHEME) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "pipeline",
        state = rememberWindowState(size = DpSize(1280.dp, 800.dp)),
    ) {
        App(initialDeepLink = deepLink)
    }
}
