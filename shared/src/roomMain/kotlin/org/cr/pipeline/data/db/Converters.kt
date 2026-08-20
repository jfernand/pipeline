/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.TypeConverter
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Room has no built-in support for kotlinx.datetime types, so both are stored as Long. */
class Converters {
    @TypeConverter
    fun epochDaysToLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.fromEpochDays(it) }

    @TypeConverter
    fun localDateToEpochDays(date: LocalDate?): Long? = date?.toEpochDays()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun instantToEpochMillis(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    /** Hash values are hex digests, so a bare comma-join is an unambiguous, dependency-free
     *  encoding — no need for JSON here. */
    @TypeConverter
    fun stringToHashList(value: String): List<String> = value.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()

    @TypeConverter
    fun hashListToString(hashes: List<String>): String = hashes.joinToString(",")
}
