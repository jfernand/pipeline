package org.cr.pipeline.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".pipeline/$DATABASE_NAME")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}
