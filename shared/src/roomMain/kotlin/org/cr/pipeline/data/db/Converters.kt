/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.TypeConverter

/** Only [EventEnvelopeEntity] is left with a column needing a converter, now that the cache
 *  tables (which needed [kotlinx.datetime.LocalDate]/[kotlin.time.Instant] converters) are gone. */
class Converters {
    /** Hash values are hex digests, so a bare comma-join is an unambiguous, dependency-free
     *  encoding — no need for JSON here. */
    @TypeConverter
    fun stringToHashList(value: String): List<String> = value.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()

    @TypeConverter
    fun hashListToString(hashes: List<String>): String = hashes.joinToString(",")
}
