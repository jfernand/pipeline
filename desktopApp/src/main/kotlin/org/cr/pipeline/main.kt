/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Taskbar
import javax.imageio.ImageIO

private const val DEEP_LINK_SCHEME = "pipeline://"

fun main(args: Array<String>) {
    // PL-039-001: Window(icon = ...)'s Window.setIconImage() never actually lands in
    // _NET_WM_ICON on this machine (confirmed via xprop against a real running instance — the
    // property is simply absent), so GNOME's dock (while the window is running) and Alt+Tab
    // switcher both fall back to a generic icon; StartupWMClass alone (the app-matching path,
    // used for the *static*/not-running dock entry, which does show the right icon) doesn't
    // cover either of those, since they prefer a window's own icon over app-matching when one's
    // available. java.awt.Taskbar is the API actually meant for "the icon the OS chrome shows for
    // this running app" — sets it once, up front, independent of any particular Window.
    setDockIcon()
    application {
        // Scanned by prefix, not position: the OS (or a wrapper script) can prepend its own
        // arguments before the actual URI (e.g. macOS Launch Services injects a -psn_... flag),
        // so args[0] is not reliably the link.
        val deepLink = args.firstOrNull { it.startsWith(DEEP_LINK_SCHEME) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "pipeline",
            state = rememberWindowState(size = DpSize(1280.dp, 800.dp)),
            icon = painterResource("icon.png"),
        ) {
            App(initialDeepLink = deepLink)
        }
    }
}

private fun setDockIcon() {
    if (!Taskbar.isTaskbarSupported()) return
    val taskbar = Taskbar.getTaskbar()
    if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return
    val image = ClassLoader.getSystemResourceAsStream("icon.png")?.use(ImageIO::read) ?: return
    taskbar.iconImage = image
}
