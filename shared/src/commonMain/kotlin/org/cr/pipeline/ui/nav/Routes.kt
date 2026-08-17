package org.cr.pipeline.ui.nav

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations, shared by both the tablet and phone entry points so they
 * navigate through the same graph shape (and, on web, the same deep-linkable URLs).
 */
@Serializable
data object ListRoute

@Serializable
data class DetailRoute(val id: Long)

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