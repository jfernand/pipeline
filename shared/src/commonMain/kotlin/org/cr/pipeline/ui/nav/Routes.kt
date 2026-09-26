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

@Serializable
data class DetailRoute(val id: Long)

/**
 * Deep-link-only (PL-041): `pipeline://app/{id}/status`. Nothing inside the app navigates here —
 * the Update status sheet is shell state, not a destination — so this route's destination just
 * redirects to [DetailRoute] with the sheet open, leaving the back stack exactly as tapping
 * Update status by hand would.
 */
@Serializable
data class UpdateStatusRoute(val id: Long)

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