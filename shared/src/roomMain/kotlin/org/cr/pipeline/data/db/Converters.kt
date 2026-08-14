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
}
