package org.cr.pipeline.data

import com.russhwolf.settings.Settings

/** Platform-native key-value storage (SharedPreferences, NSUserDefaults, java.util.prefs,
 *  localStorage) via multiplatform-settings — one implementation per real platform, rather than
 *  hand-rolling Room tables and browser storage the way ApplicationStateStore/EventLog do. Those
 *  needed platform-specific persistence *shapes* (relational vs. a flat log); this is exactly the
 *  flat key-value case the library already solves well. */
expect fun createPreferencesSettings(): Settings
