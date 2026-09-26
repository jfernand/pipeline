/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.nav

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations, shared by both the tablet and phone entry points so they
 * navigate through the same graph shape (and, on web, the same deep-linkable URLs).
 */
/**
 * [q], [status] and [exclude] preset the list's search text and status chips (PL-041), so a deep
 * link — `pipeline://list?q=eng&status=interviewing&exclude=rejected` — can land on a filtered
 * list. All three are optional: plain `pipeline://list`, and every in-app navigation to the list,
 * still land on it unfiltered. [status] and [exclude] are comma-separated status names, read by
 * [org.cr.pipeline.ui.components.parseStatusFilters].
 */
@Serializable
data class ListRoute(val q: String? = null, val status: String? = null, val exclude: String? = null)

/**
 * [sheet] names a sheet to open over the detail screen on arrival (PL-041) — sub-state carried
 * alongside the id, since the sheets are shell overlays rather than destinations of their own.
 * Optional, so `pipeline://app/{id}` and every in-app navigation still land on the bare detail
 * screen; `pipeline://app/42?sheet=status` lands with the Update status sheet up, `?sheet=contact`
 * with the Add contact sheet. A plain String
 * rather than [DetailSheet] itself so an unrecognized value still reaches the detail screen
 * instead of failing the link; [toDetailSheet] reads it.
 */
@Serializable
data class DetailRoute(val id: Long, val sheet: String? = null)

/** The sheets [DetailRoute.sheet] can name. Delete joins this when its link ships. */
enum class DetailSheet { STATUS, CONTACT }

fun String?.toDetailSheet(): DetailSheet? = DetailSheet.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

/** [id] is null when creating a new application, set when editing an existing one. */
@Serializable
data class AddEditRoute(val id: Long? = null)

@Serializable
data object FollowUpsRoute

@Serializable
data object SyncRoute

@Serializable
data object SettingsRoute

@Serializable
data object PairRoute

@Serializable
data object DevToolsRoute