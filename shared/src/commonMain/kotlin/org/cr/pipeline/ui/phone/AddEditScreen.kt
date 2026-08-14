package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.components.Field
import org.cr.pipeline.ui.components.PlFilterChip
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun AddEditScreen(modifier: Modifier = Modifier, onClose: () -> Unit = {}) {
    Column(modifier.fillMaxSize().background(PlColors.bgBase)) {
        PlTopBar(
            title = "New application",
            leftIcon = Icons.Filled.Close,
            onLeftClick = onClose,
            actionLabel = "Save",
            onAction = onClose,
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Field("Company", value = "Northwind Labs")
            Field("Role", value = "Staff Android Engineer")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MonoText("Status", size = 9.5f.sp, color = PlColors.fgMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlFilterChip("Wishlist", dotColor = AppStatus.WISHLIST.color)
                    PlFilterChip("Applied", active = true)
                    PlFilterChip("Phone screen", dotColor = AppStatus.SCREEN.color)
                    PlFilterChip("Interviewing", dotColor = AppStatus.INTERVIEW.color)
                    PlFilterChip("Offer", dotColor = AppStatus.OFFER.color)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Field("Date applied", value = "Jun 3, 2026", icon = Icons.Filled.CalendarToday, modifier = Modifier.weight(1f))
                Field("Next action", value = "Jun 21, 2026", icon = Icons.Filled.CalendarToday, modifier = Modifier.weight(1f))
            }
            Field("Posting URL", value = "northwindlabs.com/careers/staff-android", icon = Icons.Filled.Link)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MonoText("Source", size = 9.5f.sp, color = PlColors.fgMuted)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(PlColors.field, RoundedCornerShape(2.dp))
                        .border(1.dp, PlColors.borderDefault, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BodyText("Referral", size = 14.sp, color = PlColors.fgPrimary, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
                }
                MonoText("Referral · LinkedIn · Company site · Recruiter · Other", size = 9.sp, color = PlColors.fgMuted)
            }
            Field("Notes", tall = true, placeholder = "Anything you'll want to remember in three weeks.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlSecondaryButton("Cancel", onClick = onClose, modifier = Modifier.weight(1f))
                PlPrimaryButton("Save application", onClick = onClose, modifier = Modifier.weight(2f))
            }
        }
    }
}
