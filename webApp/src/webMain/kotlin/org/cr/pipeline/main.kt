package org.cr.pipeline

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

// TODO: bind the NavController to the browser's URL/history once navigation-compose ships a
// stable web-history API (checked as of navigation-compose 2.9.2 / 2.10.0-alpha02: not present
// yet). The route graph itself (see App()'s onNavHostReady hook and org.cr.pipeline.ui.nav.Routes)
// is already structured so wiring it in later is a one-line change here, not a re-architecture.
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        App()
    }
}
