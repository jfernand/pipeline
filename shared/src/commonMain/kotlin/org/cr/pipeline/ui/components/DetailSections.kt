package org.cr.pipeline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.StatusHistoryEntry
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

/** The "Status history" timeline block shown on both phone and tablet detail views. */
@Composable
fun StatusHistorySection(statusHistory: List<StatusHistoryEntry>, modifier: Modifier = Modifier) {
    if (statusHistory.isEmpty()) return
    DetailSection(
        label = "Status history",
        right = { MonoText(changesLabel(statusHistory.size), size = 9.sp, color = PlColors.fgMuted) },
        modifier = modifier,
    ) {
        Timeline(entries = statusHistory.map { TimelineEntry(it.status, it.date, it.note, it.current) })
    }
}

/** The "Contacts" list block shown on both phone and tablet detail views. */
@Composable
fun ContactsSection(contacts: List<ContactSummary>, modifier: Modifier = Modifier) {
    if (contacts.isEmpty()) return
    DetailSection(
        label = "Contacts",
        right = { Icon(Icons.Filled.Add, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp)) },
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            contacts.forEach { contact ->
                ContactRow(ContactInfo(contact.name.contactInitials(), contact.name, contact.role, contact.email))
            }
        }
    }
}
