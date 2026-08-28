/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.AttachmentId

/** Shared in-memory [FileArchiveService] fake — used anywhere a test needs a real
 *  [org.cr.pipeline.data.EventSourcedJobApplicationRepository] without touching a real archive
 *  file, including from other source sets (e.g. jvmTest) that depend on commonTest. Records every
 *  call so a test can assert not just the resulting state but what actually happened. */
class FakeFileArchiveService : FileArchiveService {
    data class WriteCall(val applicationId: ApplicationId, val attachmentId: AttachmentId, val kind: AttachmentKind, val fileName: String)

    val writes = mutableListOf<WriteCall>()
    val deletes = mutableListOf<Pair<ApplicationId, AttachmentId>>()
    private val entries = mutableMapOf<String, ByteArray>()

    override val isSupported: Boolean = true

    override suspend fun write(applicationId: ApplicationId, attachmentId: AttachmentId, kind: AttachmentKind, fileName: String, bytes: ByteArray) {
        writes += WriteCall(applicationId, attachmentId, kind, fileName)
        entries[path(applicationId, attachmentId, kind, fileName)] = bytes
    }

    override suspend fun delete(applicationId: ApplicationId, attachmentId: AttachmentId) {
        deletes += applicationId to attachmentId
        entries.keys.removeAll { key -> key.startsWith("${applicationId.value}/") && key.substringAfterLast('/').startsWith("${attachmentId.value}-") }
    }

    override suspend fun listEntries(): List<FileArchiveEntry> =
        entries.map { (path, bytes) -> FileArchiveEntry(path, bytes.size.toLong()) }

    private fun path(applicationId: ApplicationId, attachmentId: AttachmentId, kind: AttachmentKind, fileName: String): String =
        "${applicationId.value}/${kind.name.lowercase()}/${attachmentId.value}-$fileName"
}
