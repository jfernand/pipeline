/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

/** The only piece that differs between js and wasmJs here — everything built on top
 *  ([BrowserDeviceIdentityStore], [BrowserEventLog]) is shared. */
internal expect fun localStorageGet(key: String): String?
internal expect fun localStorageSet(key: String, value: String)
