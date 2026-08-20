/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.browser.localStorage

internal actual fun localStorageGet(key: String): String? = localStorage.getItem(key)

internal actual fun localStorageSet(key: String, value: String) {
    localStorage.setItem(key, value)
}
