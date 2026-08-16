package org.cr.pipeline.data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun createPreferencesSettings(): Settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
