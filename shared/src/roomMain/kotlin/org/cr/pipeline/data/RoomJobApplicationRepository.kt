package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.cr.pipeline.data.db.Application as ApplicationEntity
import org.cr.pipeline.data.db.ApplicationDao
import org.cr.pipeline.data.db.ApplicationStatus
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.sampleApplications

/** Room-backed on Android/JVM/iOS. Seeds the same sample data used by [InMemoryJobApplicationRepository]
 *  on first run, so the app has something to show before real applications are added. */
internal class RoomJobApplicationRepository(private val dao: ApplicationDao) : JobApplicationRepository {
    override fun observeApplications(): Flow<List<JobApplication>> = flow {
        if (dao.count() == 0) dao.insertAll(seedApplications())
        emitAll(dao.observeAll().map { entities -> entities.map { it.toUiModel() } })
    }
}

private fun ApplicationEntity.toUiModel(): JobApplication {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val daysAgo = dateApplied?.let { today.toEpochDays() - it.toEpochDays() } ?: 0
    val overdueDays = nextActionDate
        ?.takeIf { it < today }
        ?.let { today.toEpochDays() - it.toEpochDays() }
    val meta = dateApplied?.let { "Applied ${it.formatShort()}" } ?: "Saved"
    return JobApplication(
        company = companyName,
        role = role,
        status = status.toUiStatus(),
        daysAgo = daysAgo.toInt(),
        meta = meta,
        overdueDays = overdueDays?.toInt(),
    )
}

private fun LocalDate.formatShort(): String {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${monthNames[month.ordinal]} $day"
}

private fun ApplicationStatus.toUiStatus(): AppStatus = when (this) {
    ApplicationStatus.WISHLIST -> AppStatus.WISHLIST
    ApplicationStatus.APPLIED -> AppStatus.APPLIED
    ApplicationStatus.PHONE_SCREEN -> AppStatus.SCREEN
    ApplicationStatus.INTERVIEWING -> AppStatus.INTERVIEW
    ApplicationStatus.OFFER -> AppStatus.OFFER
    ApplicationStatus.REJECTED -> AppStatus.REJECTED
    ApplicationStatus.WITHDRAWN -> AppStatus.WITHDRAWN
}

private fun AppStatus.toDbStatus(): ApplicationStatus = when (this) {
    AppStatus.WISHLIST -> ApplicationStatus.WISHLIST
    AppStatus.APPLIED -> ApplicationStatus.APPLIED
    AppStatus.SCREEN -> ApplicationStatus.PHONE_SCREEN
    AppStatus.INTERVIEW -> ApplicationStatus.INTERVIEWING
    AppStatus.OFFER -> ApplicationStatus.OFFER
    AppStatus.REJECTED -> ApplicationStatus.REJECTED
    AppStatus.WITHDRAWN -> ApplicationStatus.WITHDRAWN
}

private fun seedApplications(): List<ApplicationEntity> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return sampleApplications.map { sample ->
        ApplicationEntity(
            companyName = sample.company,
            role = sample.role,
            status = sample.status.toDbStatus(),
            dateApplied = today.minus(sample.daysAgo, DateTimeUnit.DAY),
            postingUrl = null,
            source = null,
            nextActionDate = sample.overdueDays?.let { today.minus(it, DateTimeUnit.DAY) },
        )
    }
}
