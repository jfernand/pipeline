package org.cr.pipeline.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.cr.pipeline.data.db.AndroidDatabaseContext

actual fun createPreferencesSettings(): Settings {
    val prefs = AndroidDatabaseContext.applicationContext.getSharedPreferences("pipeline_preferences", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
