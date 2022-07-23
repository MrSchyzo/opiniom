package com.mrschyzo.opiniom.types

import com.mrschyzo.opiniom.types.Result.Companion.asResult
import com.mrschyzo.opiniom.types.Result.Companion.eitherify
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class ResultTest {

    @Test
    fun `exception-throwing block is captured in Err`() {
        val exception = Exception()
        val throwing: () -> Unit = spyk({throw exception})

        expectThat(Result.from(throwing))
            .isEqualTo(Err(exception))
        verify(exactly = 1) { throwing.invoke() }
    }

    @Test
    fun `successful block result is captured in Ok`() {
        val successful: () -> Int = spyk({120})

        expectThat(Result.from(successful))
            .isEqualTo(Ok(120))
        verify(exactly = 1) { successful.invoke() }
    }

    @Test
    fun `kotlin result failure is convertible into an Err`() {
        val exception = OutOfMemoryError()
        val ktResult = kotlin.Result.failure<Int>(exception)

        expectThat(Result.from(ktResult))
            .isEqualTo(Err(exception))
            .isEqualTo(ktResult.eitherify())
    }

    @Test
    fun `kotlin result success is convertible into an Ok`() {
        val ktResult = kotlin.Result.success(10)

        expectThat(Result.from(ktResult))
            .isEqualTo(Ok(10))
            .isEqualTo(ktResult.eitherify())
    }

    @Test
    fun `Ok is converted into a Kotlin Result success`() {
        expectThat(Ok<Int, Exception>(10).asResult())
            .isEqualTo(kotlin.Result.success(10))
    }

    @Test
    fun `Err is converted into a Kotlin Result failure`() {
        val ex = Exception()

        expectThat(Err<Int, Exception>(ex).asResult())
            .isEqualTo(kotlin.Result.failure(ex))
    }
}
