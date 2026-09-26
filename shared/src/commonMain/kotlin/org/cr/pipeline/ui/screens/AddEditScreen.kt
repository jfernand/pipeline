/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.io.FilePicker
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.model.AttachmentSummary
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.ui.components.DateField
import org.cr.pipeline.ui.components.Field
import org.cr.pipeline.ui.components.PlFilterChip
import org.cr.pipeline.ui.components.PlIconButton
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.StatusPickerFlowRow
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.koin.compose.koinInject

private val SOURCE_OPTIONS = listOf("Referral", "LinkedIn", "Company site", "Recruiter", "Other")

/** Create (applicationId == null) or edit an existing application. */
@Composable
fun AddEditScreen(
    applicationId: Long?,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    val repository = koinInject<JobApplicationRepository>()
    val filePicker = koinInject<FilePicker>()
    val scope = rememberCoroutineScope()
    val isEditing = applicationId != null

    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AppStatus.APPLIED) }
    var dateApplied by remember { mutableStateOf(if (isEditing) null else todayDate()) }
    var nextActionDate by remember { mutableStateOf<LocalDate?>(null) }
    var postingUrl by remember { mutableStateOf("") }
    var source by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(applicationId) {
        if (applicationId == null) return@LaunchedEffect
        // PL-041: pipeline://app/{id}/edit can name any id — one that never existed, or one
        // that's since been deleted. There's nothing to edit either way, and saving the blank
        // form would append an ApplicationEdited to a deleted application's history, so close
        // instead of showing it.
        val input = repository.getApplicationInput(applicationId)
        if (input == null) {
            onClose()
            return@LaunchedEffect
        }
        company = input.company
        role = input.role
        status = input.status
        dateApplied = input.dateApplied
        nextActionDate = input.nextActionDate
        postingUrl = input.postingUrl.orEmpty()
        source = input.source
        notes = input.notes
    }

    fun save() {
        scope.launch {
            repository.saveApplication(
                applicationId,
                ApplicationInput(
                    company = company.trim(),
                    role = role.trim(),
                    status = status,
                    dateApplied = dateApplied,
                    nextActionDate = nextActionDate,
                    postingUrl = postingUrl.trim().ifBlank { null },
                    source = source,
                    notes = notes.trim(),
                ),
            )
            onClose()
        }
    }

    Column(modifier.fillMaxSize().background(PlColors.bgBase)) {
        PlTopBar(
            title = if (isEditing) "Edit application" else "New application",
            leftIcon = Icons.Filled.Close,
            onLeftClick = onClose,
            actionLabel = "Save",
            onAction = ::save,
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Field("Company", value = company, placeholder = "Company name", onValueChange = { company = it })
            Field("Role", value = role, placeholder = "Job title", onValueChange = { role = it })
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MonoText("Status", size = 9.5f.sp)
                StatusPickerFlowRow(selected = status, onSelect = { status = it })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DateField(
                    "Date applied",
                    date = dateApplied,
                    onDateChange = { dateApplied = it },
                    modifier = Modifier.weight(1f),
                )
                DateField(
                    "Next action",
                    date = nextActionDate,
                    onDateChange = { nextActionDate = it },
                    modifier = Modifier.weight(1f),
                )
            }
            Field(
                "Posting URL",
                value = postingUrl,
                placeholder = "company.com/careers/role",
                icon = Icons.Filled.Link,
                onValueChange = { postingUrl = it },
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MonoText("Source", size = 9.5f.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SOURCE_OPTIONS.forEach { option ->
                        PlFilterChip(option, active = source == option, onClick = { source = option })
                    }
                }
            }
            Field(
                "Notes",
                tall = true,
                value = notes,
                placeholder = "Anything you'll want to remember in three weeks.",
                onValueChange = { notes = it },
            )
            if (applicationId != null) {
                AttachmentsSection(applicationId, repository, filePicker)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoText("Attachments", size = 9.5f.sp)
                    BodyText("Save the application first — attachments need it to have an id.", size = 12.5f.sp, color = PlColors.fgMuted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlSecondaryButton("Cancel", onClick = onClose, modifier = Modifier.weight(1f))
                PlPrimaryButton(if (isEditing) "Save changes" else "Save application", onClick = ::save, modifier = Modifier.weight(2f))
            }
        }
    }
}

/** PL-018: attach/remove a résumé, cover letter, or misc file, and see what's already there.
 *  [applicationId] must be non-null — [AddEditScreen] only shows this once the application has
 *  one, since every attach call needs an id to attach to. */
@Composable
private fun AttachmentsSection(
    applicationId: Long,
    repository: JobApplicationRepository,
    filePicker: FilePicker,
    modifier: Modifier = Modifier,
) {
    val detail by repository.observeApplicationDetail(applicationId).collectAsState(initial = null)
    val attachments = detail?.attachments.orEmpty()
    val scope = rememberCoroutineScope()

    fun attach(kind: AttachmentKind) {
        scope.launch {
            val picked = filePicker.pickFile() ?: return@launch
            when (kind) {
                AttachmentKind.RESUME -> repository.attachResume(applicationId, picked.fileName, picked.bytes)
                AttachmentKind.COVER_LETTER -> repository.attachCoverLetter(applicationId, picked.fileName, picked.bytes)
                AttachmentKind.MISC -> repository.attachFile(applicationId, picked.fileName, picked.bytes)
            }
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MonoText("Attachments", size = 9.5f.sp)
        if (!filePicker.isSupported) {
            BodyText("Not available on this platform yet.", size = 12.5f.sp, color = PlColors.fgMuted)
        } else {
            if (attachments.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                        .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp)),
                ) {
                    attachments.forEach { attachment ->
                        AttachmentRow(attachment, onRemove = { scope.launch { repository.removeAttachment(applicationId, attachment.id) } })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlSecondaryButton(
                    "Résumé",
                    icon = Icons.Filled.AttachFile,
                    onClick = { attach(AttachmentKind.RESUME) },
                    height = 40.dp,
                    modifier = Modifier.weight(1f),
                )
                PlSecondaryButton(
                    "Cover letter",
                    icon = Icons.Filled.AttachFile,
                    onClick = { attach(AttachmentKind.COVER_LETTER) },
                    height = 40.dp,
                    modifier = Modifier.weight(1f),
                )
                PlSecondaryButton(
                    "File",
                    icon = Icons.Filled.AttachFile,
                    onClick = { attach(AttachmentKind.MISC) },
                    height = 40.dp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AttachmentRow(attachment: AttachmentSummary, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().drawBottomBorder(PlColors.borderSubtle).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MonoText(attachment.kind.label(), size = 8.5f.sp, modifier = Modifier.width(84.dp))
        BodyText(
            attachment.fileName,
            size = 13.sp,
            color = PlColors.fgPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        PlIconButton(
            Icons.Filled.Close,
            onClick = onRemove,
            size = 28.dp,
            iconSize = 14.dp,
            tint = PlColors.fgMuted,
            contentDescription = "Remove ${attachment.fileName}",
        )
    }
}

private fun AttachmentKind.label(): String = when (this) {
    AttachmentKind.RESUME -> "Résumé"
    AttachmentKind.COVER_LETTER -> "Cover letter"
    AttachmentKind.MISC -> "File"
}
