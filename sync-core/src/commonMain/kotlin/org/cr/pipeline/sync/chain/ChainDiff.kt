/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.chain

/**
 * The structural result of comparing two chain tips: where they last agreed, and what each side
 * has that the other doesn't. Deliberately doesn't interpret [localOnly]/[remoteOnly] — resolving
 * a conflict between them is a separate concern (and, per this app's design, a decision left to
 * the user, not this module).
 *
 * [localOnly] and [remoteOnly] are ordered oldest-first, i.e. in replay order.
 */
data class ChainDiff(
    val commonAncestor: Hash?,
    val localOnly: List<EventEnvelope>,
    val remoteOnly: List<EventEnvelope>,
)

/**
 * Walks both chains back from their tips to find where they last agreed, and what each side has
 * seen since. [store] must contain every envelope reachable from either tip; envelopes missing
 * from it are silently treated as unknown (their ancestry stops there).
 *
 * Built for this app's actual shape — a handful of per-device chains that occasionally fork once
 * and later merge — not as a general git-style merge-base algorithm: when several common hashes
 * exist, the one closest to both tips wins, which is unambiguous for that shape but isn't
 * guaranteed to be *the* unique merge base in an arbitrarily tangled DAG.
 */
fun diffChains(store: Map<Hash, EventEnvelope>, localTip: Hash?, remoteTip: Hash?): ChainDiff {
    val localDistances = ancestryDistances(store, localTip)
    val remoteDistances = ancestryDistances(store, remoteTip)

    val commonAncestor = localDistances.keys.intersect(remoteDistances.keys)
        .minByOrNull { localDistances.getValue(it) + remoteDistances.getValue(it) }

    val localOnly = localDistances.keys - remoteDistances.keys
    val remoteOnly = remoteDistances.keys - localDistances.keys

    return ChainDiff(
        commonAncestor = commonAncestor,
        localOnly = localOnly.mapNotNull(store::get).sortedByDescending { localDistances.getValue(it.hash) },
        remoteOnly = remoteOnly.mapNotNull(store::get).sortedByDescending { remoteDistances.getValue(it.hash) },
    )
}

/** Every hash reachable from [tip] via [EventEnvelope.parentHashes], mapped to its shortest
 *  distance (in hops) back from [tip]. Follows every parent, not just the first, so merge
 *  envelopes' ancestry on both sides is included. */
private fun ancestryDistances(store: Map<Hash, EventEnvelope>, tip: Hash?): Map<Hash, Int> {
    if (tip == null) return emptyMap()
    val distances = mutableMapOf<Hash, Int>()
    val queue = ArrayDeque<Pair<Hash, Int>>()
    queue.add(tip to 0)
    while (queue.isNotEmpty()) {
        val (hash, distance) = queue.removeFirst()
        val shortestSoFar = distances[hash]
        if (shortestSoFar != null && shortestSoFar <= distance) continue
        distances[hash] = distance
        val envelope = store[hash] ?: continue
        envelope.parentHashes.forEach { parent -> queue.add(parent to distance + 1) }
    }
    return distances
}
