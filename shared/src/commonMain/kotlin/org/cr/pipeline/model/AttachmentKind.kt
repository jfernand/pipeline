/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import kotlinx.serialization.Serializable

/** What [org.cr.pipeline.sync.event.ResumeAttached]/[org.cr.pipeline.sync.event.CoverLetterAttached]/
 *  [org.cr.pipeline.sync.event.FileAttached] attach. [RESUME] and [COVER_LETTER] are
 *  one-slot-per-application — adding a new one replaces the prior one of the same kind; [MISC] is
 *  a list, every add just appends. */
@Serializable
enum class AttachmentKind { RESUME, COVER_LETTER, MISC }
