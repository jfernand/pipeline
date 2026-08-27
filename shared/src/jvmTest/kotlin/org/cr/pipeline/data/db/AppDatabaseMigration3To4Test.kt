/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies the steps in the event-log-as-source-of-truth and PL-019 changes that could go
 * destructively wrong if they did: opening a real pre-existing version-3 database (applications/
 * status_events/contacts/reminders still present, alongside event_envelopes) must run the
 * hand-written MIGRATION_3_4 (drops only the four cache tables) and MIGRATION_4_5 (adds logKind,
 * defaulted for every pre-existing row) — not fallbackToDestructiveMigration, which would wipe
 * event_envelopes too, on releases meant to make it durable and then keep it that way.
 *
 * Builds the "old" file with raw SQL rather than the deleted Room entity classes (gone from the
 * codebase now that the cache is in-memory-only) — table content doesn't matter to a `DROP TABLE`,
 * only that the tables and `event_envelopes`' real rows exist and `user_version` reads 3, exactly
 * as a real pre-existing install's file would. Everything downstream — [buildDatabase],
 * [AppDatabase] — is the actual, unmodified production code.
 */
class AppDatabaseMigration3To4Test {
    private val dbFile = File.createTempFile("pipeline-migration-test", ".db")

    @AfterTest
    fun cleanUp() {
        dbFile.delete()
    }

    private fun rawConnection(): SQLiteConnection = BundledSQLiteDriver().open(dbFile.absolutePath)

    private fun buildVersion3Database() {
        rawConnection().use { connection ->
            connection.execSQL("CREATE TABLE applications (id INTEGER PRIMARY KEY, companyName TEXT)")
            connection.execSQL("CREATE TABLE status_events (id INTEGER PRIMARY KEY, applicationId INTEGER)")
            connection.execSQL("CREATE TABLE contacts (id INTEGER PRIMARY KEY, applicationId INTEGER)")
            connection.execSQL("CREATE TABLE reminders (id INTEGER PRIMARY KEY, applicationId INTEGER)")
            connection.execSQL(
                "CREATE TABLE event_envelopes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, hash TEXT NOT NULL, " +
                    "parentHashes TEXT NOT NULL, deviceId TEXT NOT NULL, sequence INTEGER NOT NULL, " +
                    "timestampEpochMillis INTEGER NOT NULL, payload TEXT NOT NULL)",
            )
            connection.execSQL(
                "INSERT INTO event_envelopes (hash, parentHashes, deviceId, sequence, timestampEpochMillis, payload) " +
                    "VALUES ('hash-1', '', 'device-legacy', 0, 1000, '{\"pre-existing\":true}')",
            )
            connection.execSQL("PRAGMA user_version = 3")
        }
    }

    private fun tableNames(): Set<String> {
        rawConnection().use { connection ->
            connection.prepare("SELECT name FROM sqlite_master WHERE type = 'table'").use { statement ->
                val names = mutableSetOf<String>()
                while (statement.step()) names += statement.getText(0)
                return names
            }
        }
    }

    @Test
    fun `opening a real version-3 database drops the cache tables but keeps event_envelopes`() = runTest {
        buildVersion3Database()
        assertTrue(tableNames().containsAll(listOf("applications", "status_events", "contacts", "reminders", "event_envelopes")))

        val db = buildDatabase(Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath))
        val envelopes = db.eventEnvelopeDao().observeAll("REAL").first()
        db.close()

        assertEquals(1, envelopes.size)
        assertEquals("hash-1", envelopes.single().hash)
        assertEquals("{\"pre-existing\":true}", envelopes.single().payload)
        assertEquals("REAL", envelopes.single().logKind, "a row that predates logKind is real device history, never demo data")

        val remaining = tableNames()
        assertTrue("event_envelopes" in remaining, "the durable event log must survive the migration")
        assertFalse(
            remaining.any { it in setOf("applications", "status_events", "contacts", "reminders") },
            "dropped cache tables must not survive",
        )
    }
}
