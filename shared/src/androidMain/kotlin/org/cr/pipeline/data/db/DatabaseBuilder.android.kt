package org.cr.pipeline.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** Set once from MainActivity before Koin resolves the database. */
object AndroidDatabaseContext {
    lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AndroidDatabaseContext.applicationContext
    val dbFile = context.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(context = context, name = dbFile.absolutePath)
}
