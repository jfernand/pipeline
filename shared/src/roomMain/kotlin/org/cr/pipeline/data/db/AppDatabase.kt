package org.cr.pipeline.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [Application::class, StatusEvent::class, Contact::class, Reminder::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao
    abstract fun statusEventDao(): StatusEventDao
    abstract fun contactDao(): ContactDao
    abstract fun reminderDao(): ReminderDao
}

/**
 * KSP generates the `actual` for this per target (Android/JVM/iOS) — Kotlin/Native can't
 * reflectively instantiate the generated Room implementation the way the JVM can.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

const val DATABASE_NAME = "pipeline.db"

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
