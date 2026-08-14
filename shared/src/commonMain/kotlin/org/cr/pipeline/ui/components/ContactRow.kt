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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors
import org.cr.pipeline.ui.theme.PipeType

data class ContactInfo(val initials: String, val name: String, val role: String, val email: String)

@Composable
fun ContactRow(contact: ContactInfo, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val avatarShape = RoundedCornerShape(2.dp)
        Box(
            Modifier.size(40.dp)
                .background(PipeColors.bgOverlay, avatarShape)
                .border(1.dp, PipeColors.borderDefault, avatarShape),
            contentAlignment = Alignment.Center,
        ) {
            MonoText(contact.initials, size = 11.sp, color = PipeColors.fgSecondary, letterSpacing = 0.05f.em)
        }
        Column(Modifier.weight(1f)) {
            BodyText(contact.name, size = 14.5f.sp, weight = FontWeight.SemiBold, color = PipeColors.fgPrimary)
            MonoText(contact.role, size = 9.sp, color = PipeColors.fgMuted, modifier = Modifier.padding(top = 3.dp))
            androidx.compose.material3.Text(
                contact.email,
                fontFamily = PipeType.mono(),
                fontSize = 11.sp,
                color = PipeColors.fgSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row {
            PipeIconButton(Icons.Filled.Email, size = 40.dp, iconSize = 16.dp)
            PipeIconButton(Icons.Filled.Link, size = 40.dp, iconSize = 16.dp)
        }
    }
}
