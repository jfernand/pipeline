@file:OptIn(ExperimentalUnsignedTypes::class)

package org.cr.pipeline.sync.chain

/**
 * A self-contained SHA-256 (FIPS 180-4) implementation. Written by hand rather than pulled in as
 * a dependency: this module deliberately has zero external dependencies, and the algorithm is
 * small, fixed, and fully covered by known test vectors — safer to own outright than to guess at
 * an unverified library coordinate for every one of this module's five targets.
 */
fun sha256(input: ByteArray): ByteArray {
    var h0 = 0x6a09e667u
    var h1 = 0xbb67ae85u
    var h2 = 0x3c6ef372u
    var h3 = 0xa54ff53au
    var h4 = 0x510e527fu
    var h5 = 0x9b05688cu
    var h6 = 0x1f83d9abu
    var h7 = 0x5be0cd19u

    val messageLengthBits = input.size.toULong() * 8u
    // Pad with 0x80, then zeros, until length ≡ 56 (mod 64), then the original bit length as a
    // big-endian 64-bit integer — bringing the total to a multiple of 64 bytes.
    val zeroPaddingCount = ((55 - input.size % 64) + 64) % 64
    val lengthBytes = ByteArray(8) { i -> ((messageLengthBits shr ((7 - i) * 8)) and 0xffu).toByte() }
    val padded = input + byteArrayOf(0x80.toByte()) + ByteArray(zeroPaddingCount) + lengthBytes

    for (chunkStart in padded.indices step 64) {
        val w = UIntArray(64)
        for (i in 0 until 16) {
            val base = chunkStart + i * 4
            w[i] = (padded[base].toUInt() and 0xffu shl 24) or
                (padded[base + 1].toUInt() and 0xffu shl 16) or
                (padded[base + 2].toUInt() and 0xffu shl 8) or
                (padded[base + 3].toUInt() and 0xffu)
        }
        for (i in 16 until 64) {
            val s0 = w[i - 15].rotr(7) xor w[i - 15].rotr(18) xor (w[i - 15] shr 3)
            val s1 = w[i - 2].rotr(17) xor w[i - 2].rotr(19) xor (w[i - 2] shr 10)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = h0
        var b = h1
        var c = h2
        var d = h3
        var e = h4
        var f = h5
        var g = h6
        var h = h7

        for (i in 0 until 64) {
            val s1 = e.rotr(6) xor e.rotr(11) xor e.rotr(25)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + s1 + ch + ROUND_CONSTANTS[i] + w[i]
            val s0 = a.rotr(2) xor a.rotr(13) xor a.rotr(22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj

            h = g
            g = f
            f = e
            e = d + temp1
            d = c
            c = b
            b = a
            a = temp1 + temp2
        }

        h0 += a
        h1 += b
        h2 += c
        h3 += d
        h4 += e
        h5 += f
        h6 += g
        h7 += h
    }

    val result = ByteArray(32)
    uintArrayOf(h0, h1, h2, h3, h4, h5, h6, h7).forEachIndexed { index, word ->
        result[index * 4] = (word shr 24).toByte()
        result[index * 4 + 1] = (word shr 16).toByte()
        result[index * 4 + 2] = (word shr 8).toByte()
        result[index * 4 + 3] = word.toByte()
    }
    return result
}

fun ByteArray.toHex(): String = joinToString("") { byte -> (byte.toInt() and 0xff).toString(16).padStart(2, '0') }

private fun UInt.rotr(bits: Int): UInt = (this shr bits) or (this shl (32 - bits))

private val ROUND_CONSTANTS: UIntArray = uintArrayOf(
    0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u, 0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
    0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u, 0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
    0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu, 0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
    0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u, 0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
    0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u, 0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
    0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u, 0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
    0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u, 0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
    0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u, 0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u,
)
