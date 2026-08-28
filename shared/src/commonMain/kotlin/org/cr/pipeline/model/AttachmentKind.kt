/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import kotlinx.serialization.Serializable

/** What an [org.cr.pipeline.sync.event.AttachmentAdded] event attaches. [RESUME] and
 *  [COVER_LETTER] are one-slot-per-application — adding a new one replaces the prior one of the
 *  same kind; [MISC] is a list, every add just appends. */
@Serializable
enum class AttachmentKind { RESUME, COVER_LETTER, MISC }
