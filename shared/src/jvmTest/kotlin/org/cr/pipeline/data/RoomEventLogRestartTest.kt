/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.cr.pipeline.data.db.AppDatabase
import org.cr.pipeline.data.db.buildDatabase
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.sync.chain.DeviceId
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The end-to-end proof for why the Room cache tables were removed in favor of replaying
 * event_envelopes: this is the exact scenario — a real application surviving a full app restart —
 * that a destructive schema migration used to threaten, since the cache and the log used to share
 * one droppable database. Uses a real on-disk SQLite file (what desktopApp actually does, unlike
 * every other test here, which stays in memory), closing and reopening [AppDatabase] against it to
 * stand in for a restart.
 */
class RoomEventLogRestartTest {
    private val deviceIdentityStore = object : DeviceIdentityStore {
        override suspend fun getDeviceId(): DeviceId = DeviceId("test-device")
    }
    private val dbFile = File.createTempFile("pipeline-restart-test", ".db")

    @AfterTest
    fun cleanUp() {
        dbFile.delete()
    }

    private fun openDatabase(): AppDatabase = buildDatabase(Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath))

    @Test
    fun `a real application survives closing and reopening the Room-backed event log`() = runTest {
        val db1 = openDatabase()
        val eventLog1 = RoomEventLog(deviceIdentityStore, db1.eventEnvelopeDao())
        val repository1 = EventSourcedJobApplicationRepository(InMemoryApplicationStateStore(eventLog1), eventLog1)
        val id = repository1.saveApplication(
            null,
            ApplicationInput(
                company = "Restart Verification Inc",
                role = "QA Engineer",
                status = AppStatus.APPLIED,
                dateApplied = LocalDate(2026, 8, 1),
                nextActionDate = null,
                postingUrl = null,
                source = null,
                notes = "",
            ),
        )
        repository1.updateStatus(id, AppStatus.OFFER, "Verbal offer")
        db1.close()

        // A fresh AppDatabase/RoomEventLog/InMemoryApplicationStateStore over the same file —
        // nothing from db1's in-memory objects carries over, only what actually made it to disk.
        val db2 = openDatabase()
        val eventLog2 = RoomEventLog(deviceIdentityStore, db2.eventEnvelopeDao())
        val applications = InMemoryApplicationStateStore(eventLog2).observeAll().first()
        db2.close()

        assertEquals(1, applications.size)
        val (restartedId, state) = applications.single()
        assertEquals(id, restartedId)
        assertEquals("Restart Verification Inc", state.company)
        assertEquals(AppStatus.OFFER, state.status)
        assertEquals(2, state.statusHistory.size)
    }
}
