/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.chain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private val deviceA = DeviceId("device-a")
private val deviceB = DeviceId("device-b")

private fun envelope(payload: String, vararg parents: String, device: DeviceId = deviceA) = EventEnvelope(
    hash = Hash(payload),
    parentHashes = parents.map { Hash(it) },
    deviceId = device,
    sequence = 0,
    timestampEpochMillis = 0,
    payload = payload,
)

class ChainDiffTest {

    @Test
    fun `identical tips are in sync`() {
        val e1 = envelope("1")
        val store = mapOf(e1.hash to e1)

        val diff = diffChains(store, localTip = e1.hash, remoteTip = e1.hash)

        assertEquals(e1.hash, diff.commonAncestor)
        assertEquals(emptyList(), diff.localOnly)
        assertEquals(emptyList(), diff.remoteOnly)
        assertEquals(ChainStatus.InSync, diff.classify())
    }

    @Test
    fun `local ahead of remote`() {
        val e1 = envelope("1")
        val e2 = envelope("2", "1")
        val store = mapOf(e1.hash to e1, e2.hash to e2)

        val diff = diffChains(store, localTip = e2.hash, remoteTip = e1.hash)

        assertEquals(e1.hash, diff.commonAncestor)
        assertEquals(listOf(e2), diff.localOnly)
        assertEquals(emptyList(), diff.remoteOnly)
        assertEquals(ChainStatus.Ahead(1), diff.classify())
    }

    @Test
    fun `local behind remote`() {
        val e1 = envelope("1")
        val e2 = envelope("2", "1")
        val store = mapOf(e1.hash to e1, e2.hash to e2)

        val diff = diffChains(store, localTip = e1.hash, remoteTip = e2.hash)

        assertEquals(e1.hash, diff.commonAncestor)
        assertEquals(emptyList(), diff.localOnly)
        assertEquals(listOf(e2), diff.remoteOnly)
        assertEquals(ChainStatus.Behind(1), diff.classify())
    }

    @Test
    fun `two devices forking after a shared ancestor diverge`() {
        // 1 -> 2 -> 3            (chain A's tip)
        //      \ -> 4 -> 5       (chain B's tip)
        val e1 = envelope("1")
        val e2 = envelope("2", "1")
        val e3 = envelope("3", "2")
        val e4 = envelope("4", "2", device = deviceB)
        val e5 = envelope("5", "4", device = deviceB)
        val store = listOf(e1, e2, e3, e4, e5).associateBy { it.hash }

        val diff = diffChains(store, localTip = e3.hash, remoteTip = e5.hash)

        assertEquals(e2.hash, diff.commonAncestor)
        assertEquals(listOf(e3), diff.localOnly)
        assertEquals(listOf(e4, e5), diff.remoteOnly)
        assertIs<ChainStatus.Diverged>(diff.classify())
    }

    @Test
    fun `a merge event reconciles both chains back to in sync`() {
        // 1 -> 2 -> 3 ------\
        //      \ -> 4 -> 5 --+--> M(parents = 3, 5)
        val e1 = envelope("1")
        val e2 = envelope("2", "1")
        val e3 = envelope("3", "2")
        val e4 = envelope("4", "2", device = deviceB)
        val e5 = envelope("5", "4", device = deviceB)
        val merge = envelope("M", "3", "5")
        val store = listOf(e1, e2, e3, e4, e5, merge).associateBy { it.hash }

        // Device A adopts the merge as its new tip; device B is still sitting on its old tip (5).
        // A is ahead by 2: the merge itself, plus "3" (its own pre-merge event B never saw).
        val diff = diffChains(store, localTip = merge.hash, remoteTip = e5.hash)
        assertEquals(ChainStatus.Ahead(2), diff.classify())

        // Once device B also adopts the merge, both tips are identical again.
        val bothOnMerge = diffChains(store, localTip = merge.hash, remoteTip = merge.hash)
        assertEquals(ChainStatus.InSync, bothOnMerge.classify())
    }

    @Test
    fun `unrelated chains have no common ancestor`() {
        val e1 = envelope("1")
        val other1 = envelope("x1")
        val store = mapOf(e1.hash to e1, other1.hash to other1)

        val diff = diffChains(store, localTip = e1.hash, remoteTip = other1.hash)

        assertNull(diff.commonAncestor)
        assertEquals(listOf(e1), diff.localOnly)
        assertEquals(listOf(other1), diff.remoteOnly)
    }

    @Test
    fun `null tip means an empty chain`() {
        val e1 = envelope("1")
        val store = mapOf(e1.hash to e1)

        val diff = diffChains(store, localTip = null, remoteTip = e1.hash)

        assertNull(diff.commonAncestor)
        assertEquals(emptyList(), diff.localOnly)
        assertEquals(listOf(e1), diff.remoteOnly)
        assertEquals(ChainStatus.Behind(1), diff.classify())
    }
}
