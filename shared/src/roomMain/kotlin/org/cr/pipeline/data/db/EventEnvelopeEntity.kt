/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** One hashed event, in the shape it's persisted — see [org.cr.pipeline.sync.chain.EventEnvelope]
 *  for the domain type this maps to/from. [parentHashes] is comma-joined (see [Converters]);
 *  hash values are hex digests and never contain a comma. [logKind] is an
 *  [org.cr.pipeline.sync.event.EventLogKind] name — a real device chain and PL-019's demo chain
 *  share this one table, kept apart by this column rather than a second table, so a schema change
 *  to one never risks the other. */
@Entity(tableName = "event_envelopes")
data class EventEnvelopeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hash: String,
    val parentHashes: List<String>,
    val deviceId: String,
    val sequence: Long,
    val timestampEpochMillis: Long,
    val payload: String,
    val logKind: String = "REAL",
)

@Dao
interface EventEnvelopeDao {
    @Query("SELECT * FROM event_envelopes WHERE logKind = :logKind ORDER BY sequence ASC")
    fun observeAll(logKind: String): Flow<List<EventEnvelopeEntity>>

    @Query("SELECT * FROM event_envelopes WHERE logKind = :logKind ORDER BY sequence DESC LIMIT 1")
    suspend fun getLast(logKind: String): EventEnvelopeEntity?

    @Insert
    suspend fun insert(envelope: EventEnvelopeEntity)
}
