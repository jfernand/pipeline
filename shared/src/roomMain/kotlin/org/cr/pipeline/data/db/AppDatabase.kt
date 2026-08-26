/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        EventEnvelopeEntity::class,
    ],
    // 4: applications/status_events/contacts/reminders dropped — that read-side cache is
    // in-memory only now, materialized by replaying event_envelopes at startup
    // (InMemoryApplicationStateStore.kt) instead of being persisted and migrated in place. The
    // event log is the only thing this database still needs to keep.
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
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

// event_envelopes must survive this bump — it's the durable source of truth applications get
// rebuilt from, so wiping it here would be exactly the bug this cache-removal is fixing, on the
// release meant to fix it. Real migration, not fallbackToDestructiveMigration, specifically so
// event_envelopes is left untouched; only the now-unused cache tables get dropped.
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS applications")
        connection.execSQL("DROP TABLE IF EXISTS status_events")
        connection.execSQL("DROP TABLE IF EXISTS contacts")
        connection.execSQL("DROP TABLE IF EXISTS reminders")
    }
}

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    // Dispatchers.IO isn't part of kotlinx-coroutines-core's common API — it's a JVM actual, and
    // restricted on Native — so it doesn't resolve here in roomMain's own common-metadata compile
    // (shared by Android/JVM/iOS). Default works everywhere this source set does; the bundled
    // SQLite driver isn't doing classic blocking file I/O the way a JDBC driver would, so there's
    // no real IO-vs-CPU distinction being lost by not having a dedicated IO pool here.
    .setQueryCoroutineContext(Dispatchers.Default)
    .addMigrations(MIGRATION_3_4)
    // Still no migration story beyond 3->4 (pre-release, exportSchema = false) — a future bump
    // recreates the local DB, including event_envelopes, unless it also gets a real migration
    // the way 3->4 did above.
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
