/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

/**
 * One overdue thing across the whole pipeline — either an application's own overdue next-action
 * date, or one of its overdue [ReminderSummary]s — flattened into a single shape so
 * [org.cr.pipeline.data.JobApplicationRepository.observeFollowUps] can list them together,
 * sorted, without the UI caring which of the two produced any given row.
 */
data class FollowUpItem(
    val applicationId: Long,
    val company: String,
    val role: String,
    val message: String,
    val dueDate: String,
)
