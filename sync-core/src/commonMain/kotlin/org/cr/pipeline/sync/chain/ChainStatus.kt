package org.cr.pipeline.sync.chain

/** The human-facing read of a [ChainDiff]: are the two chains in sync, and if not, who has what. */
sealed interface ChainStatus {
    data object InSync : ChainStatus
    data class Ahead(val by: Int) : ChainStatus
    data class Behind(val by: Int) : ChainStatus
    data class Diverged(val diff: ChainDiff) : ChainStatus
}

fun ChainDiff.classify(): ChainStatus = when {
    localOnly.isEmpty() && remoteOnly.isEmpty() -> ChainStatus.InSync
    localOnly.isNotEmpty() && remoteOnly.isEmpty() -> ChainStatus.Ahead(localOnly.size)
    localOnly.isEmpty() && remoteOnly.isNotEmpty() -> ChainStatus.Behind(remoteOnly.size)
    else -> ChainStatus.Diverged(this)
}
