package org.cr.pipeline.model

import kotlinx.datetime.LocalDate

/** The editable fields of an application, as gathered by the Add/Edit form. */
data class ApplicationInput(
    val company: String,
    val role: String,
    val status: AppStatus,
    val dateApplied: LocalDate?,
    val nextActionDate: LocalDate?,
    val postingUrl: String?,
    val source: String?,
    val notes: String,
)
