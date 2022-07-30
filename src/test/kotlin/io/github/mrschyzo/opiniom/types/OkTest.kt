package io.github.mrschyzo.opiniom.types

import io.github.mrschyzo.opiniom.functional.andThen
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotSameInstanceAs
import strikt.assertions.isSameInstanceAs

internal class OkTest {

    @Test
    fun `unwrapping an Ok returns the Left value`() {
        expectThat(Ok<Int, Nothing>(10).unwrap())
            .isEqualTo(10)
    }

    @Test
    fun `Ok is Ok, DUH!`() {
        expectThat(Ok<Int, Nothing>(10))
            .assertThat("Ok.isOk == true", Result<Int, Nothing>::isOk)
            .assertThat("Ok.isErr == false", Result<Int, Nothing>::isErr andThen Boolean::not)
    }

    @Test
    fun `ifErr does nothing if Ok`() {
        val block = spyk({ _: Any -> })

        expectThat(Ok<Int, Any>(10).ifErr(block))
            .isA<Unit>()
        verify(exactly = 0) { block.invoke(any()) }
    }

    @Test
    fun `runErr does nothing if Ok but it returns a new copy of Ok`() {
        val block = spyk({ _: Any -> })
        val input = Ok<Int, Any>(10)

        expectThat(input.runErr(block))
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { block.invoke(any()) }
    }

    @Test
    fun `ifOk runs closure only once if Ok`() {
        val block = spyk({ _: Int -> })

        expectThat(Ok<Int, Any>(10).ifOk(block))
            .isA<Unit>()
        verify(exactly = 1) { block.invoke(eq(10)) }
    }

    @Test
    fun `runOk runs closure only once if Ok and it returns a new copy of Ok`() {
        val block = spyk({ _: Int -> })
        val input = Ok<Int, Any>(10)

        expectThat(input.runOk(block))
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 1) { block.invoke(eq(10)) }
    }

    @Test
    fun `mapErr does nothing if Ok but it returns a new Ok with different Right type`() {
        val transformation = spyk({ _: Any -> 15.0 })

        expectThat(Ok<Int, Any>(10).mapErr(transformation))
            .isA<Ok<Int, Double>>()
            .isEqualTo(Ok(10))
        verify(exactly = 0) { transformation.invoke(any()) }
    }

    @Test
    fun `flatmapErr does nothing if Ok but it returns a new Ok with different Right type`() {
        val output = Ok<Int, String>(118)
        val transformation = spyk({ _: Any -> output })

        expectThat(Ok<Int, Any>(10).flatmapErr(transformation))
            .isA<Ok<Int, String>>()
            .isEqualTo(Ok(10))
        verify(exactly = 0) { transformation.invoke(any()) }
    }

    @Test
    fun `mapOk returns a new Ok transformed Left content`() {
        val transformation = spyk(Int::toDouble)

        expectThat(Ok<Int, Any>(10).mapOk(transformation))
            .isA<Ok<Double, Any>>()
            .isEqualTo(Ok(10.0))
        verify(exactly = 1) { transformation.invoke(eq(10)) }
    }

    @Test
    fun `flatmapOk returns a new Err with same Right type`() {
        val output = Err<String, String>("")
        val transformation = spyk({ _: Int -> output })

        expectThat(Ok<Int, String>(10).flatmapOk(transformation))
            .isEqualTo(output)
            .isSameInstanceAs(output)
        verify(exactly = 1) { transformation.invoke(eq(10)) }
    }

    @Test
    fun `flatmapOk returns a new Ok with same Right type`() {
        val output = Ok<Double, String>(420.69)
        val transformation = spyk({ _: Int -> output })

        expectThat(Ok<Int, String>(10).flatmapOk(transformation))
            .isEqualTo(output)
            .isSameInstanceAs(output)
        verify(exactly = 1) { transformation.invoke(eq(10)) }
    }

    @Test
    fun `Ok and Ok = second Ok`() {
        val input = Ok<String, Int>("ciao")
        val other = Ok<String, Int>("10.0")

        expectThat(input and other)
            .isSameInstanceAs(other)
            .isEqualTo(other)
    }

    @Test
    fun `Ok and Err = Err`() {
        val input = Ok<String, Int>("10.0")
        val other = Err<String, Int>(10)

        expectThat(input and other)
            .isSameInstanceAs(other)
            .isEqualTo(other)
    }

    @Test
    fun `Ok lazy-and Ok = Ok returned by producer`() {
        val input = Ok<String, Int>("ciao")
        val other = Ok<String, Int>("10.0")
        val producer = spyk({ other })

        expectThat(input and producer)
            .isSameInstanceAs(other)
            .isEqualTo(other)
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `Ok lazy-and Err = Err returned by producer`() {
        val input = Ok<String, Int>("10")
        val other = Err<String, Int>(420)
        val producer = spyk({ other })

        expectThat(input and producer)
            .isSameInstanceAs(other)
            .isEqualTo(other)
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `Ok or Ok = copy of first Ok`() {
        val input = Ok<String, Int>("ciao")
        val other = Ok<String, Int>("10.0")

        expectThat(input or other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Ok or Err = copy of first Ok`() {
        val input = Ok<String, Int>("ciao")
        val other = Err<String, Int>(420)

        expectThat(input or other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Ok lazy-or Ok = copy of first Ok with short-circuiting`() {
        val input = Ok<String, Int>("ciao")
        val other = Ok<String, Int>("10.0")
        val producer = spyk({ other })

        expectThat(input or producer)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `Ok lazy-or Err = copy of first Ok with short-circuiting`() {
        val input = Ok<String, Int>("ciao")
        val other = Err<String, Int>(420)
        val producer = spyk({ other })

        expectThat(input or producer)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `match returns 'ok' closure result if Ok, 'err' closure is ignored`() {
        val err = spyk(Any::toString)
        val ok = spyk(Int::unaryMinus)

        val result = Ok<Int, Any>(192).match(
            ok = ok,
            err = err
        )

        expectThat(result).isEqualTo(-192)
        verify(exactly = 0) { err.invoke(any()) }
        verify(exactly = 1) { ok.invoke(eq(192)) }
    }

    @Test
    fun `Ok as kotlin Result type wraps its Left value in a kotlin Result success`() {
        expectThat(Ok<Int, Double>(120).asKtResult())
            .isEqualTo(kotlin.Result.success(120))
    }

    @Test
    fun `Ok returns Some if trying to extract Ok`() {
        expectThat(Ok<Int, Double>(120).extractOk())
            .isEqualTo(Some(120))
    }

    @Test
    fun `Ok returns None if trying to extract Err`() {
        expectThat(Ok<Int, Double>(120).extractErr())
            .isA<None<Double>>()
    }
}
