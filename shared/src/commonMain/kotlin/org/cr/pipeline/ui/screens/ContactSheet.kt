/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.ContactInput
import org.cr.pipeline.ui.components.Field
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun ContactSheet(
    company: String,
    role: String,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSave: (ContactInput) -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    var contactRole by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(PlColors.bgRaised, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .border(1.dp, PlColors.borderDefault, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.size(width = 32.dp, height = 4.dp).background(PlColors.borderStrong, RoundedCornerShape(2.dp)))
        }
        Column {
            DisplayText("Add contact", size = 21.sp, letterSpacing = 0.02f.em)
            BodyText(
                "$company · $role",
                size = 13.sp,
                color = PlColors.fgMuted,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Field(label = "Name", value = name, placeholder = "Jane Doe", onValueChange = { name = it })
        Field(label = "Role", value = contactRole, placeholder = "Recruiter", onValueChange = { contactRole = it })
        Field(label = "Email", value = email, placeholder = "jane@company.com", onValueChange = { email = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlSecondaryButton("Cancel", onClick = onCancel, height = 46.dp, modifier = Modifier.weight(1f))
            PlPrimaryButton(
                "Save contact",
                onClick = { onSave(ContactInput(name, contactRole, email)) },
                height = 46.dp,
                modifier = Modifier.weight(2f),
            )
        }
    }
}
