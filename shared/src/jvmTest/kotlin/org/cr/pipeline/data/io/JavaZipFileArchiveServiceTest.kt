/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.io.File
import kotlinx.coroutines.test.runTest
import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.AttachmentId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Exercises the real archive file on disk — what desktopApp actually does — rather than mocking
 *  `java.util.zip`, since the whole point of this class is the read-all/rewrite-all round trip
 *  those APIs force. */
class JavaZipFileArchiveServiceTest {
    // Deleted immediately: JavaZipFileArchiveService must create this fresh on first write, the
    // same as it would on a real device's first-ever attachment.
    private val archiveFile = File.createTempFile("pipeline-files-test", ".zip").apply { delete() }
    private val service = JavaZipFileArchiveService(archiveFile)

    @AfterTest
    fun cleanUp() {
        archiveFile.delete()
    }

    @Test
    fun `listEntries is empty when the archive file doesn't exist yet`() = runTest {
        assertEquals(emptyList(), service.listEntries())
    }

    @Test
    fun `write then listEntries shows the entry at the expected path with its size`() = runTest {
        val applicationId = ApplicationId("app-1")
        val attachmentId = AttachmentId("attachment-1")

        service.write(applicationId, attachmentId, AttachmentKind.RESUME, "resume.pdf", "hello world".encodeToByteArray())

        val entries = service.listEntries()
        assertEquals(1, entries.size)
        assertEquals("app-1/resume/attachment-1-resume.pdf", entries.single().path)
        assertEquals("hello world".encodeToByteArray().size.toLong(), entries.single().sizeBytes)
    }

    @Test
    fun `delete removes only the matching entry, leaving others in the archive untouched`() = runTest {
        val applicationId = ApplicationId("app-1")
        val keep = AttachmentId("keep-me")
        val remove = AttachmentId("remove-me")
        service.write(applicationId, keep, AttachmentKind.MISC, "keep.pdf", "keep".encodeToByteArray())
        service.write(applicationId, remove, AttachmentKind.MISC, "remove.pdf", "remove".encodeToByteArray())

        service.delete(applicationId, remove)

        val entries = service.listEntries()
        assertEquals(1, entries.size)
        assertTrue(entries.single().path.contains("keep-me"))
    }

    @Test
    fun `delete for an entry that was never written is a no-op`() = runTest {
        val applicationId = ApplicationId("app-1")
        service.write(applicationId, AttachmentId("real"), AttachmentKind.MISC, "real.pdf", "bytes".encodeToByteArray())

        service.delete(applicationId, AttachmentId("never-written"))

        assertEquals(1, service.listEntries().size)
    }

    @Test
    fun `write replaces an entry written under the same path`() = runTest {
        val applicationId = ApplicationId("app-1")
        val attachmentId = AttachmentId("attachment-1")
        service.write(applicationId, attachmentId, AttachmentKind.RESUME, "resume.pdf", "v1".encodeToByteArray())

        service.write(applicationId, attachmentId, AttachmentKind.RESUME, "resume.pdf", "v2".encodeToByteArray())

        val entries = service.listEntries()
        assertEquals(1, entries.size)
        assertEquals("v2".encodeToByteArray().size.toLong(), entries.single().sizeBytes)
    }
}
