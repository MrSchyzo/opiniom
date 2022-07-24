package com.github.mrschyzo.opiniom.types

import com.github.mrschyzo.opiniom.functional.andThen
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotSameInstanceAs
import strikt.assertions.isSameInstanceAs

internal class ErrTest {

    @Test
    fun `unwrapping an Err results in a ResultUnwrapException`() {
        expectThrows<ResultUnwrapException> { Err<Nothing, Int>(10).unwrap() }
            .assertThat("Exception must contain value") {
                it.value == 10
            }
    }

    @Test
    fun `Err is not Ok, DUH!`() {
        expectThat(Err<Nothing, Int>(10))
            .assertThat("Err.isOk == false", Result<Nothing, Int>::isOk andThen Boolean::not)
            .assertThat("Err.isErr == true", Result<Nothing, Int>::isErr)
    }

    @Test
    fun `ifOk does nothing if Err`() {
        val block = spyk({ _: Any -> })

        expectThat(Err<Any, Int>(10).ifOk(block))
            .isA<Unit>()
        verify(exactly = 0) { block.invoke(any()) }
    }

    @Test
    fun `runOk does nothing if Err but it returns a new copy of Err`() {
        val block = spyk({ _: Any -> })
        val input = Err<Any, Int>(10)

        expectThat(input.runOk(block))
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { block.invoke(any()) }
    }

    @Test
    fun `ifErr runs closure only once if Err`() {
        val block = spyk({ _: Any -> })

        expectThat(Err<Any, Int>(10).ifErr(block))
            .isA<Unit>()
        verify(exactly = 1) { block.invoke(eq(10)) }
    }

    @Test
    fun `runErr runs closure only once if Err and it returns a new copy of Err`() {
        val block = spyk({ _: Any -> })
        val input = Err<Any, Int>(10)

        expectThat(input.runErr(block))
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 1) { block.invoke(eq(10)) }
    }

    @Test
    fun `mapOk does nothing if Err but it returns a new Err with different Left type`() {
        val transformation = spyk({ _: Any -> 15.0 })

        expectThat(Err<Any, Int>(10).mapOk(transformation))
            .isA<Err<Double, Int>>()
            .isEqualTo(Err(10))
        verify(exactly = 0) { transformation.invoke(any()) }
    }

    @Test
    fun `flatmapOk does nothing if Err but it returns a new Err with different Left type`() {
        val output = Ok<String, Int>("Ciao")
        val transformation = spyk({ _: Any -> output })

        expectThat(Err<Any, Int>(10).flatmapOk(transformation))
            .isA<Err<String, Int>>()
            .isEqualTo(Err(10))
        verify(exactly = 0) { transformation.invoke(any()) }
    }

    @Test
    fun `mapErr returns a new Err transformed Right content`() {
        val transformation = spyk(Int::toDouble)

        expectThat(Err<Any, Int>(10).mapErr(transformation))
            .isA<Err<Any, Double>>()
            .isEqualTo(Err(10.0))
        verify(exactly = 1) { transformation.invoke(eq(10)) }
    }

    @Test
    fun `flatmapErr returns a new Err with same Left type`() {
        val output = Err<String, String>("")
        val transformation = spyk({ _: Int -> output })

        expectThat(Err<String, Int>(10).flatmapErr(transformation))
            .isEqualTo(output)
            .isSameInstanceAs(output)
        verify(exactly = 1) { transformation.invoke(eq(10)) }
    }

    @Test
    fun `flatmapErr returns a new Ok with same Left type`() {
        val output = Ok<String, Double>("Ciao")
        val transformation = spyk({ _: Int -> output })

        expectThat(Err<String, Int>(10).flatmapErr(transformation))
            .isEqualTo(output)
            .isSameInstanceAs(output)
        verify(exactly = 1) { transformation.invoke(eq(10)) }
    }

    @Test
    fun `Err and Ok = copy of Err`() {
        val input = Err<String, Int>(10)
        val other = Ok<String, Int>("10.0")

        expectThat(input and other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Err and Err = copy of first Err`() {
        val input = Err<String, Int>(10)
        val other = Err<String, Int>(420)

        expectThat(input and other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Err lazy-and Ok = copy of Err with short-circuiting`() {
        val input = Err<String, Int>(10)
        val other = Ok<String, Int>("10.0")
        val producer = spyk({ other })

        expectThat(input and producer)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `Err lazy-and Err = copy of first Err with short-circuiting`() {
        val input = Err<String, Int>(10)
        val other = Err<String, Int>(420)
        val producer = spyk({ other })

        expectThat(input and producer)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `Err or Ok = Ok`() {
        val input = Err<String, Int>(10)
        val other = Ok<String, Int>("10.0")

        expectThat(input or other)
            .isSameInstanceAs(other)
            .isEqualTo(other)
    }

    @Test
    fun `Err or Err = second err`() {
        val input = Err<String, Int>(10)
        val other = Err<String, Int>(420)

        expectThat(input or other)
            .isSameInstanceAs(other)
            .isEqualTo(other)
    }

    @Test
    fun `Err lazy-or Ok = Ok returned by producer`() {
        val input = Err<String, Int>(10)
        val other = Ok<String, Int>("10.0")
        val producer = spyk({ other })

        expectThat(input or producer)
            .isSameInstanceAs(other)
            .isEqualTo(other)
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `Err lazy-or Err = second Err returned by producer`() {
        val input = Err<String, Int>(10)
        val other = Err<String, Int>(420)
        val producer = spyk({ other })

        expectThat(input or producer)
            .isSameInstanceAs(other)
            .isEqualTo(other)
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `match returns 'err' closure result if Err, 'ok' closure is ignored`() {
        val ok = spyk(Any::toString)
        val err = spyk(Int::unaryMinus)

        val result = Err<Any, Int>(192).match(
            ok = ok,
            err = err
        )

        expectThat(result).isEqualTo(-192)
        verify(exactly = 0) { ok.invoke(any()) }
        verify(exactly = 1) { err.invoke(eq(192)) }
    }

    @Test
    fun `Err as kotlin Result type wraps its Right value in a ResultUnwrapException`() {
        expectThat(Err<Double, Int>(120).asKtResult())
            .isEqualTo(kotlin.Result.failure(ResultUnwrapException(120)))
    }

    @Test
    fun `Err returns None if trying to extract Ok`() {
        expectThat(Err<Double, Int>(120).extractOk())
            .isA<None<Double>>()
    }

    @Test
    fun `Err returns Some if trying to extract Err`() {
        expectThat(Err<Double, Int>(120).extractErr())
            .isEqualTo(Some(120))
    }
}
