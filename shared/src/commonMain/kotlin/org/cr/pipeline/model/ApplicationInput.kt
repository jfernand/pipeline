/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** The editable fields of an application, as gathered by the Add/Edit form. */
@Serializable
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

/** The fields gathered by the "Add contact" sheet. */
@Serializable
data class ContactInput(
    val name: String,
    val role: String,
    val email: String,
)
