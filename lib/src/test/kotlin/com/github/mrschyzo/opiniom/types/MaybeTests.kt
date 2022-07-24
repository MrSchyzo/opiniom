package com.github.mrschyzo.opiniom.types

import com.github.mrschyzo.opiniom.types.Maybe.Companion.flatten
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

internal class MaybeTests {

    @Test
    fun `None comes out from a null value`() {
        expectThat(Maybe.from<Int>(null))
            .isA<None<Int>>()
    }

    @Test
    fun `Some comes out from a non-null value`() {
        expectThat(Maybe.from(128))
            .isEqualTo(Some(128))
    }

    @Test
    fun `flatten a Some(None()) returns None()`() {
        val input: Maybe<Maybe<Any>> = Some(None())

        expectThat(input.flatten())
            .isA<None<Any>>()
    }

    @Test
    fun `flatten a Some(Some(10)) returns Some(10)`() {
        val input: Maybe<Maybe<Int>> = Some(Some(10))

        expectThat(input.flatten())
            .isA<Some<Int>>()
            .isEqualTo(Some(10))
    }
}
