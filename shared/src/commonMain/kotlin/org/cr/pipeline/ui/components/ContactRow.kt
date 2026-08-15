package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType

data class ContactInfo(val initials: String, val name: String, val role: String, val email: String)

fun String.contactInitials(): String =
    trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).map { it.first().uppercaseChar() }.joinToString("")

@Composable
fun ContactRow(contact: ContactInfo, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val avatarShape = RoundedCornerShape(2.dp)
        Box(
            Modifier.size(40.dp)
                .background(PlColors.bgOverlay, avatarShape)
                .border(1.dp, PlColors.borderDefault, avatarShape),
            contentAlignment = Alignment.Center,
        ) {
            MonoText(contact.initials, size = 11.sp, color = PlColors.fgSecondary, letterSpacing = 0.05f.em)
        }
        Column(Modifier.weight(1f)) {
            BodyText(
                contact.name,
                size = 14.5f.sp,
                weight = FontWeight.SemiBold,
                color = PlColors.fgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MonoText(
                contact.role,
                size = 9.sp,
                color = PlColors.fgMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
            androidx.compose.material3.Text(
                contact.email,
                fontFamily = PlType.mono(),
                fontSize = 11.sp,
                color = PlColors.fgSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row {
            PlIconButton(Icons.Filled.Email, size = 40.dp, iconSize = 16.dp)
            PlIconButton(Icons.Filled.Link, size = 40.dp, iconSize = 16.dp)
        }
    }
}
