package org.cr.pipeline

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform