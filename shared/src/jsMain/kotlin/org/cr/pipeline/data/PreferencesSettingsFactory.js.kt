/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual fun createPreferencesSettings(): Settings = StorageSettings()
