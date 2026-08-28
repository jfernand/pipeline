/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.AttachmentId

/**
 * PL-031: owns the on-disk life of every file an [org.cr.pipeline.sync.event.AttachmentAdded]/
 * [org.cr.pipeline.sync.event.AttachmentRemoved] event records — [EventSourcedJobApplicationRepository]
 * (see [org.cr.pipeline.data.EventSourcedJobApplicationRepository]) calls this alongside appending
 * the event itself, the same way it calls [org.cr.pipeline.data.ApplicationStateStore]. This is
 * dumb, business-logic-free storage — no event construction, no single-slot-per-kind enforcement
 * (that's [org.cr.pipeline.sync.event.applyEvent]'s job) — just bytes in, bytes gone.
 *
 * Only JVM and Android have a real implementation today; iOS/js/wasmJs each get
 * [UnsupportedFileArchiveService], same rollout shape as
 * [org.cr.pipeline.data.io.DataPortController].
 */
interface FileArchiveService {
    val isSupported: Boolean

    /** Writes [bytes] under [applicationId]/[kind]/[attachmentId], overwriting nothing — a caller
     *  enforcing a single-slot kind (résumé, cover letter) must [delete] the prior entry itself. */
    suspend fun write(applicationId: ApplicationId, attachmentId: AttachmentId, kind: AttachmentKind, fileName: String, bytes: ByteArray)

    /** No-ops if [attachmentId] isn't present — deleting an application leaves its attachments in
     *  the archive alone (PL-031), so this is only ever called for an explicit detach. */
    suspend fun delete(applicationId: ApplicationId, attachmentId: AttachmentId)

    /** Every entry currently in the archive, for Dev Tools' Files viewer — not scoped to one
     *  application, since that viewer shows the archive's raw contents, not one application's. */
    suspend fun listEntries(): List<FileArchiveEntry>
}

data class FileArchiveEntry(val path: String, val sizeBytes: Long)

/** [org.cr.pipeline.di.fileArchiveModule] resolves this once, on startup, the same way
 *  [org.cr.pipeline.data.io.createDataPortController] does for [DataPortController]. */
expect fun createFileArchiveService(): FileArchiveService

/** Shared by every platform without a real implementation yet. */
object UnsupportedFileArchiveService : FileArchiveService {
    override val isSupported: Boolean = false

    override suspend fun write(applicationId: ApplicationId, attachmentId: AttachmentId, kind: AttachmentKind, fileName: String, bytes: ByteArray) {}
    override suspend fun delete(applicationId: ApplicationId, attachmentId: AttachmentId) {}
    override suspend fun listEntries(): List<FileArchiveEntry> = emptyList()
}
