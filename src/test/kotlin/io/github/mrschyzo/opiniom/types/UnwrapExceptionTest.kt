package io.github.mrschyzo.opiniom.types

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class UnwrapExceptionTest {
    @Test
    fun `message is as expected`() {
        val ex = UnwrapException()

        expectThat(ex.message)
            .isEqualTo("Unwrapping failed")
    }
}
