/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.platform.linux

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

data class LinuxUrlSchemeRegistration(
    val appName: String,
    val desktopFileName: String,
    val executablePath: String,
    val scheme: String,
    val iconPath: String? = null,
    val categories: String = "Utility;",
)

fun registerLinuxUrlScheme(config: LinuxUrlSchemeRegistration) {
    require(config.scheme.matches(Regex("[a-z][a-z0-9+.-]*"))) {
        "Invalid URL scheme: ${config.scheme}"
    }

    val home = System.getProperty("user.home")
    val applicationsDir = Path.of(home, ".local", "share", "applications")
    Files.createDirectories(applicationsDir)

    val desktopFile = applicationsDir.resolve(config.desktopFileName)

    val escapedExecutable = quoteDesktopExecArg(config.executablePath)

    val desktopContent = buildString {
        appendLine("[Desktop Entry]")
        appendLine("Name=${config.appName}")
        appendLine("Type=Application")
        appendLine("Exec=$escapedExecutable %u")
        appendLine("Terminal=false")
        appendLine("NoDisplay=false")
        appendLine("Categories=${config.categories}")
        appendLine("MimeType=x-scheme-handler/${config.scheme};")

        if (!config.iconPath.isNullOrBlank()) {
            appendLine("Icon=${config.iconPath}")
        }
    }

    Files.writeString(
        desktopFile,
        desktopContent,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE,
    )

    desktopFile.toFile().setReadable(true, false)

    runCommand(
        "xdg-mime",
        "default",
        config.desktopFileName,
        "x-scheme-handler/${config.scheme}",
    )

    runCommandIfAvailable(
        "update-desktop-database",
        applicationsDir.toString(),
    )
}

private fun quoteDesktopExecArg(value: String): String {
    // Desktop Entry Exec values support quoting with double quotes.
    // Escape backslash, double quote, dollar, and backtick for safety.
    val escaped = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("$", "\\$")
        .replace("`", "\\`")

    return "\"$escaped\""
}

private fun runCommand(vararg command: String) {
    val process = ProcessBuilder(*command)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()

    if (exitCode != 0) {
        error(
            """
            Command failed with exit code $exitCode:
            ${command.joinToString(" ")}
            
            Output:
            $output
            """.trimIndent()
        )
    }
}

private fun runCommandIfAvailable(vararg command: String) {
    try {
        runCommand(*command)
    } catch (_: java.io.IOException) {
        // Command not installed. This is common/minor on some distros.
    }
}
