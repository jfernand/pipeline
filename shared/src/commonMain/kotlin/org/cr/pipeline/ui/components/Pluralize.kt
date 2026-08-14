package org.cr.pipeline.ui.components

fun changesLabel(count: Int): String = if (count == 1) "1 change" else "$count changes"
