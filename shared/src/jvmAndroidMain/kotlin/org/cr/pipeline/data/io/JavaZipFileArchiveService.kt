/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.AttachmentId

/**
 * Shared by JVM and Android (`jvmAndroidMain`) — both are plain `java.util.zip`/`java.io`, and
 * NIO2's zip-filesystem provider isn't reliably available on Android/ART, so this sticks to the
 * one API surface both platforms actually have rather than needing two implementations.
 *
 * `java.util.zip.ZipOutputStream` can't modify an existing archive in place, so [write] and
 * [delete] both read every entry into memory, apply the one change, and rewrite the whole file —
 * fine at the scale of résumés/cover letters/misc documents this archive holds, not something
 * meant to scale to large media libraries.
 */
class JavaZipFileArchiveService(private val archiveFile: File) : FileArchiveService {
    private val mutex = Mutex()

    override val isSupported: Boolean = true

    override suspend fun write(applicationId: ApplicationId, attachmentId: AttachmentId, kind: AttachmentKind, fileName: String, bytes: ByteArray) {
        val path = entryPath(applicationId, attachmentId, kind, fileName)
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val entries = readEntries().toMutableMap()
                entries[path] = bytes
                writeEntries(entries)
            }
        }
    }

    override suspend fun delete(applicationId: ApplicationId, attachmentId: AttachmentId) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val entries = readEntries()
                val remaining = entries.filterKeys { path -> !matches(path, applicationId, attachmentId) }
                if (remaining.size != entries.size) writeEntries(remaining)
            }
        }
    }

    override suspend fun listEntries(): List<FileArchiveEntry> = withContext(Dispatchers.IO) {
        mutex.withLock {
            readEntries().map { (path, bytes) -> FileArchiveEntry(path, bytes.size.toLong()) }
        }
    }

    private fun matches(path: String, applicationId: ApplicationId, attachmentId: AttachmentId): Boolean =
        path.startsWith("${applicationId.value}/") && path.substringAfterLast('/').startsWith("${attachmentId.value}-")

    private fun readEntries(): Map<String, ByteArray> {
        if (!archiveFile.exists()) return emptyMap()
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(archiveFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        }
        return entries
    }

    private fun writeEntries(entries: Map<String, ByteArray>) {
        archiveFile.parentFile?.mkdirs()
        ZipOutputStream(archiveFile.outputStream()).use { zip ->
            entries.forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
    }
}

private fun entryPath(applicationId: ApplicationId, attachmentId: AttachmentId, kind: AttachmentKind, fileName: String): String =
    "${applicationId.value}/${kind.name.lowercase()}/${attachmentId.value}-$fileName"
