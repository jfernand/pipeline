/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.toApplicationInput
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.EventEnvelope
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.ContactAdded
import org.cr.pipeline.sync.event.ContactId
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.EventProvenance

/**
 * PL-019: wraps a fresh, empty demo [EventLog] (see [org.cr.pipeline.sync.event.EventLogKind]),
 * seeding it from [seedApplications] the first time anything reads from or writes to it, if it's
 * still empty. One real [ApplicationCreated] event per seed entry, followed by one [ContactAdded]
 * per entry in that seed's contacts — the same shapes a user creating that application and adding
 * its contacts by hand would produce — so every application this store ever holds, seeded or not,
 * replays correctly. Once seeded, this behaves exactly like the [delegate] it wraps; nothing here
 * runs a second time.
 *
 * Lazy rather than run once at startup, and gated by a [Mutex] the same way
 * [InMemoryApplicationStateStore]'s own materialization is — so this works identically on every
 * platform, including js/wasmJs, where there's no blocking suspend call available at DI-module
 * composition time to do this eagerly instead.
 */
class DemoSeedingEventLog(private val delegate: EventLog) : EventLog {
    private val seedMutex = Mutex()
    private var seeded = false

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return@withLock
            if (delegate.observeChain().first().isEmpty()) {
                val provenance = EventProvenance.Device(delegate.deviceId())
                val today = todayDate()
                var timestamp = Clock.System.now().toEpochMilliseconds()
                for (seed in seedApplications) {
                    val applicationId = ApplicationId.random()
                    val created: ApplicationEvent = ApplicationCreated(applicationId, seed.toApplicationInput(today), provenance)
                    delegate.append(created, timestamp++)
                    for (contact in seed.contacts) {
                        val contactAdded: ApplicationEvent = ContactAdded(applicationId, ContactId.random(), contact, provenance)
                        delegate.append(contactAdded, timestamp++)
                    }
                }
            }
            seeded = true
        }
    }

    override suspend fun deviceId(): DeviceId = delegate.deviceId()

    override fun observeChain(): Flow<List<EventEnvelope>> = flow {
        ensureSeeded()
        emitAll(delegate.observeChain())
    }

    override suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope {
        ensureSeeded()
        return delegate.append(event, timestampEpochMillis)
    }
}
