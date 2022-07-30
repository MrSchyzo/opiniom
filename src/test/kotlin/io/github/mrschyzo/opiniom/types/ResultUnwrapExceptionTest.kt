package io.github.mrschyzo.opiniom.types

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class ResultUnwrapExceptionTest {

    @Test
    fun `exception contains a value`() {
        val ex = ResultUnwrapException(6)

        expectThat(ex.value)
            .isEqualTo(6)
    }

    @Test
    fun `exception has the expected message`() {
        val ex = ResultUnwrapException(6)

        expectThat(ex.message)
            .isEqualTo("Unsuccessfully unwrapping a result: 6")
    }
}
