/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        Application::class,
        StatusEvent::class,
        Contact::class,
        Reminder::class,
        DeviceIdentity::class,
        EventEnvelopeEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao
    abstract fun statusEventDao(): StatusEventDao
    abstract fun contactDao(): ContactDao
    abstract fun reminderDao(): ReminderDao
    abstract fun deviceIdentityDao(): DeviceIdentityDao
    abstract fun eventEnvelopeDao(): EventEnvelopeDao
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
    // Dispatchers.IO isn't part of kotlinx-coroutines-core's common API — it's a JVM actual, and
    // restricted on Native — so it doesn't resolve here in roomMain's own common-metadata compile
    // (shared by Android/JVM/iOS). Default works everywhere this source set does; the bundled
    // SQLite driver isn't doing classic blocking file I/O the way a JDBC driver would, so there's
    // no real IO-vs-CPU distinction being lost by not having a dedicated IO pool here.
    .setQueryCoroutineContext(Dispatchers.Default)
    // No migration story yet (pre-release, exportSchema = false) — a schema bump just recreates
    // the local DB. Seed data repopulates automatically; nothing durable is lost that a future
    // synced install couldn't recover.
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
