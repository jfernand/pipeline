/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createPreferencesSettings(): Settings = PreferencesSettings(Preferences.userRoot().node("org/cr/pipeline"))
