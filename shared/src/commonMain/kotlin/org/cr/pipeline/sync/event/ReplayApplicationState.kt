/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.chain.EventEnvelope

/**
 * Reconstructs every application's current state by folding the whole event log through
 * [applyEvent], instead of that reducer only ever seeing one new event at a time the way
 * [org.cr.pipeline.data.EventSourcedJobApplicationRepository] calls it. This is what makes the
 * event log a real source of truth rather than an audit trail nothing reads back: any storage
 * holding only [envelopes] can recover full application state from nothing else.
 *
 * [envelopes] must already be in chronological order (every [EventLog] implementation guarantees
 * this — sequence-ordered for Room, append-order for the in-memory/browser ones).
 *
 * Ids are assigned by encounter order of each application's [ApplicationCreated] event (the Nth
 * one seen gets id N) — deterministic and stable across repeated replays of the same log, with no
 * separate bookkeeping needed. Ascending — newest-first display ordering is the caller's job.
 *
 * Tolerant, not strict: a payload that fails to decode (malformed JSON, or an [org.cr.pipeline.model.AppStatus]
 * name this build doesn't recognize — see the append-only convention documented there) or an
 * edit/status-change with no prior create for its [ApplicationEvent.applicationId] gets reported
 * to [onSkippedEnvelope] and skipped, rather than [applyEvent]'s internal `checkNotNull` throwing
 * and losing every other application in the same replay to one bad envelope.
 */
fun replayApplicationState(
    envelopes: List<EventEnvelope>,
    today: LocalDate = todayDate(),
    onSkippedEnvelope: (EventEnvelope, Throwable) -> Unit = { _, _ -> },
): List<Pair<Long, ApplicationState>> {
    val idOrder = LinkedHashMap<ApplicationId, Long>()
    val states = mutableMapOf<ApplicationId, ApplicationState>()

    for (envelope in envelopes) {
        val event = runCatching { Json.decodeFromString<ApplicationEvent>(envelope.payload) }
            .getOrElse { onSkippedEnvelope(envelope, it); continue }
        when (event) {
            is ApplicationCreated -> {
                if (event.applicationId in idOrder) {
                    onSkippedEnvelope(envelope, IllegalStateException("Duplicate ApplicationCreated for ${event.applicationId}"))
                    continue
                }
                idOrder[event.applicationId] = idOrder.size + 1L
                states[event.applicationId] = applyEvent(null, event, today)
            }
            is ApplicationEdited, is StatusChanged, is ContactAdded -> {
                val current = states[event.applicationId]
                if (current == null) {
                    onSkippedEnvelope(envelope, IllegalStateException("${event::class.simpleName} for ${event.applicationId} with no prior create"))
                    continue
                }
                states[event.applicationId] = applyEvent(current, event, today)
            }
        }
    }

    return idOrder.map { (applicationId, id) -> id to states.getValue(applicationId) }
}
