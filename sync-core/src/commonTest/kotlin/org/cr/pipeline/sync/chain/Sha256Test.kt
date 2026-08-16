package org.cr.pipeline.sync.chain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** Every expected digest below was produced locally with `sha256sum`/`printf`, not recalled from
 *  memory — these are ground truth, not "vectors that look about right." */
class Sha256Test {

    @Test
    fun `empty input`() {
        assertHash("", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    }

    @Test
    fun `short input`() {
        assertHash("abc", "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
    }

    @Test
    fun `pangram-length input`() {
        assertHash(
            "The quick brown fox jumps over the lazy dog",
            "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592",
        )
    }

    @Test
    fun `input exactly at the padding boundary`() {
        // 55 bytes: input.size % 64 == 55, the largest size that still fits in a single block.
        assertHash("a".repeat(55), "9f4390f8d30c2dd92ec9f095b65e2b9ae9b0a925a5258e241c9f1e910f734318")
        // 56 bytes: one more forces a second block purely for padding.
        assertHash("a".repeat(56), "b35439a4ac6f0948b6d6f9e3c6af0f5f590ce20f1bde7090ef7970686ec6738a")
    }

    @Test
    fun `input spanning exactly one full block`() {
        assertHash("a".repeat(64), "ffe054fe7ae0cb6dc65c3af9b61d5209f439851db43d0ba5997337df154668eb")
    }

    @Test
    fun `input spanning two blocks`() {
        assertHash("a".repeat(100), "2816597888e4a0d3a36b82b83316ab32680eb8f00f8cd3b904d681246d285a0e")
    }

    @Test
    fun `different inputs never collide in practice`() {
        assertNotEquals(sha256("a".encodeToByteArray()).toHex(), sha256("b".encodeToByteArray()).toHex())
    }

    private fun assertHash(input: String, expectedHex: String) {
        assertEquals(expectedHex, sha256(input.encodeToByteArray()).toHex())
    }
}
