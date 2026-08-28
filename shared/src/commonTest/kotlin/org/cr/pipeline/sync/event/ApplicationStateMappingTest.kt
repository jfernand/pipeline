/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.cr.pipeline.model.AppStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationStateMappingTest {
    private val today = LocalDate(2026, 8, 25)

    private fun LocalDate.ago(days: Int) = minus(days, DateTimeUnit.DAY)
    private fun LocalDate.ahead(days: Int) = plus(days, DateTimeUnit.DAY)

    private fun state(
        applicationId: String,
        company: String,
        nextActionDate: LocalDate? = null,
        reminders: List<ReminderRecord> = emptyList(),
    ) = ApplicationState(
        applicationId = ApplicationId(applicationId),
        company = company,
        role = "Engineer",
        status = AppStatus.APPLIED,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = nextActionDate,
        postingUrl = null,
        source = null,
        notes = "",
        statusHistory = emptyList(),
        contacts = emptyList(),
        reminders = reminders,
        attachments = emptyList(),
    )

    @Test
    fun `an application with nothing overdue produces no items`() {
        val state = state("a", "Cedar & Byrne", nextActionDate = today.ahead(3))

        assertEquals(emptyList(), listOf(1L to state).toFollowUpItems(today))
    }

    @Test
    fun `an overdue next-action date with no reminders produces one item`() {
        val state = state("a", "Cedar & Byrne", nextActionDate = today.ago(3))

        val items = listOf(1L to state).toFollowUpItems(today)

        assertEquals(1, items.size)
        assertEquals("Next action due", items.single().message)
        assertEquals(1L, items.single().applicationId)
    }

    @Test
    fun `overdue reminders each produce their own item, upcoming ones none`() {
        val state = state(
            "a", "Cedar & Byrne",
            reminders = listOf(
                ReminderRecord("Nudge recruiter", today.ago(1)),
                ReminderRecord("Prep for round 2", today.ahead(2)),
            ),
        )

        val items = listOf(1L to state).toFollowUpItems(today)

        assertEquals(listOf("Nudge recruiter"), items.map { it.message })
    }

    @Test
    fun `an overdue next-action date and an overdue reminder are not deduplicated`() {
        val overdueDate = today.ago(3)
        val state = state(
            "a", "Cedar & Byrne",
            nextActionDate = overdueDate,
            reminders = listOf(ReminderRecord("Email Dana about round 2 timing", overdueDate)),
        )

        val items = listOf(1L to state).toFollowUpItems(today)

        assertEquals(2, items.size)
    }

    @Test
    fun `items from every application are merged and sorted by due date`() {
        val states = listOf(
            1L to state("a", "Cedar & Byrne", reminders = listOf(ReminderRecord("Older", today.ago(1)))),
            2L to state("b", "Northwind Labs", reminders = listOf(ReminderRecord("Oldest", today.ago(5)))),
        )

        val items = states.toFollowUpItems(today)

        assertEquals(listOf("Oldest", "Older"), items.map { it.message })
        assertEquals(listOf("Northwind Labs", "Cedar & Byrne"), items.map { it.company })
    }
}
