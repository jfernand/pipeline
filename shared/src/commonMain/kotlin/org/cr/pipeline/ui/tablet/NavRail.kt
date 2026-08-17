package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.BuildInfo
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawRightBorder

enum class NavDestination(val icon: ImageVector, val label: String) {
    LIST(Icons.Filled.Layers, "List"),
    FOLLOWUPS(Icons.Filled.Notifications, "Follow-ups"),
    SYNC(Icons.Filled.QrCode, "Sync"),
    SETTINGS(Icons.Filled.Settings, "Settings"),
}

@Composable
fun NavRail(active: NavDestination, onSelect: (NavDestination) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .width(84.dp)
            .fillMaxHeight()
            .background(PlColors.bgSunken)
            .drawRightBorder(PlColors.borderDefault)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DisplayText(
            "PL",
            size = 30.sp,
            color = PlColors.brandPrimary,
            letterSpacing = 0.04f.em,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
        )
        NavDestination.entries.forEach { destination ->
            if (destination == NavDestination.SETTINGS) return@forEach
            val isActive = destination == active
            val itemShape = RoundedCornerShape(2.dp)
            DestinationIcon(destination, itemShape, isActive, onSelect)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Dot(color = AppStatus.OFFER.color)
            MonoText("Synced", size = 8.sp, color = PlColors.fgMuted)
            MonoText(
                BuildInfo.GIT_DESCRIBE,
                size = 6.5f.sp,
                color = PlColors.fgMuted,
                letterSpacing = 0f.em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DestinationIcon(
    destination: NavDestination,
    itemShape: RoundedCornerShape,
    isActive: Boolean,
    onSelect: (NavDestination) -> Unit,
) {
    Column(
        Modifier
            .padding(vertical = 2.dp)
            .width(68.dp)
            .clip(itemShape)
            .background(if (isActive) PlColors.brandSubtle else Color.Transparent)
            .border(1.dp, if (isActive) PlColors.overdueBorder else Color.Transparent, itemShape)
            .clickable { onSelect(destination) }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            destination.icon,
            contentDescription = destination.label,
            tint = if (isActive) PlColors.brandPrimary else PlColors.fgMuted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(6.dp))
        MonoText(destination.label, size = 8.5f.sp, color = if (isActive) PlColors.brandPrimary else PlColors.fgMuted)
    }
}
