package org.cr.pipeline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.ui.components.Field
import org.cr.pipeline.ui.components.PlFilterChip
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.koinInject

private val SOURCE_OPTIONS = listOf("Referral", "LinkedIn", "Company site", "Recruiter", "Other")

/** Create (applicationId == null) or edit an existing application. Dates are entered as
 *  ISO-8601 (YYYY-MM-DD) — there's no calendar picker yet, just a plain validated text field. */
@Composable
fun AddEditScreen(
    applicationId: Long?,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    val repository = koinInject<JobApplicationRepository>()
    val scope = rememberCoroutineScope()
    val isEditing = applicationId != null

    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AppStatus.APPLIED) }
    var dateAppliedText by remember { mutableStateOf(if (isEditing) "" else todayDate().toString()) }
    var nextActionText by remember { mutableStateOf("") }
    var postingUrl by remember { mutableStateOf("") }
    var source by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(applicationId) {
        if (applicationId == null) return@LaunchedEffect
        repository.getApplicationInput(applicationId)?.let { input ->
            company = input.company
            role = input.role
            status = input.status
            dateAppliedText = input.dateApplied?.toString().orEmpty()
            nextActionText = input.nextActionDate?.toString().orEmpty()
            postingUrl = input.postingUrl.orEmpty()
            source = input.source
            notes = input.notes
        }
    }

    fun save() {
        scope.launch {
            repository.saveApplication(
                applicationId,
                ApplicationInput(
                    company = company.trim(),
                    role = role.trim(),
                    status = status,
                    dateApplied = dateAppliedText.toLocalDateOrNull(),
                    nextActionDate = nextActionText.toLocalDateOrNull(),
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
                MonoText("Status", size = 9.5f.sp, color = PlColors.fgMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppStatus.entries.forEach { entry ->
                        val isSelected = entry == status
                        PlFilterChip(
                            entry.label,
                            active = isSelected,
                            dotColor = if (isSelected) null else entry.color,
                            onClick = { status = entry },
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Field(
                    "Date applied",
                    value = dateAppliedText,
                    placeholder = "YYYY-MM-DD",
                    icon = Icons.Filled.CalendarToday,
                    onValueChange = { dateAppliedText = it },
                    modifier = Modifier.weight(1f),
                )
                Field(
                    "Next action",
                    value = nextActionText,
                    placeholder = "YYYY-MM-DD",
                    icon = Icons.Filled.CalendarToday,
                    onValueChange = { nextActionText = it },
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
                MonoText("Source", size = 9.5f.sp, color = PlColors.fgMuted)
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlSecondaryButton("Cancel", onClick = onClose, modifier = Modifier.weight(1f))
                PlPrimaryButton(if (isEditing) "Save changes" else "Save application", onClick = ::save, modifier = Modifier.weight(2f))
            }
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = trim().takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
