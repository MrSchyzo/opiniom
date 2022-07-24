package com.github.mrschyzo.opiniom.types

import com.github.mrschyzo.opiniom.functional.andThen
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotSameInstanceAs
import strikt.assertions.isSameInstanceAs

internal class SomeTest {
    @Test
    fun `unwrapping a Some returns the contained value`() {
        expectThat(Some(10).unwrap())
            .isEqualTo(10)
    }

    @Test
    fun `orError-ing a Some returns the contained value in an Ok`() {
        expectThat(Some(10).orError(7))
            .isEqualTo(Ok(10))
    }

    @Test
    fun `lazy onError-ing a Some never calls the producer`() {
        val producer = spyk({ "ERROR" })

        expectThat(Some(10).orError(producer))
            .isEqualTo(Ok(10))
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `eager orElseThrow-ing a Some doesn't throw any exception`() {
        expectThat(Some(10).orElseThrow(Exception()))
            .isEqualTo(10)
    }

    @Test
    fun `lazy orElseThrow-ing a Some never calls the producer and unwraps the value`() {
        val producer = spyk({ OutOfMemoryError() })

        expectThat(Some(20).orElseThrow(producer))
            .isEqualTo(20)
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `orElse-ing a Some returns the contained value`() {
        expectThat(Some("10").orElse("Fallback"))
            .isEqualTo("10")
    }

    @Test
    fun `lazy orElse-ing a Some nevr calls the producer and returns the contained value`() {
        val producer = spyk({ "fallback" })

        expectThat(Some("contained").orElse(producer))
            .isEqualTo("contained")
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `Some is a Some, DUH!`() {
        expectThat(Some(10))
            .assertThat("Returns true for isSome", Maybe<Int>::isSome)
            .assertThat("Returns false for isNone", Maybe<Int>::isNone andThen Boolean::not)
    }

    @Test
    fun `match-ing a Some only calls once the 'some' closure, 'none' closure is not called at all`() {
        val noneClosure = spyk({ "abcde" })
        val someClosure = spyk({ _: String -> "y" })

        expectThat(Some("succ").match(some = someClosure, none = noneClosure))
            .isEqualTo("y")
        verify(exactly = 0) { noneClosure.invoke() }
        verify(exactly = 1) { someClosure.invoke(eq("succ")) }
    }

    @Test
    fun `calling ifNone with Some does nothing`() {
        val block = spyk({})

        expectThat(Some("7").ifNone(block))
            .isEqualTo(Unit)
        verify(exactly = 0) { block.invoke() }
    }

    @Test
    fun `calling runNone with Some does nothing but returns a new copy of Some`() {
        val block = spyk({})
        val input = Some(120)

        expectThat(input.runNone(block))
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 0) { block.invoke() }
    }

    @Test
    fun `calling ifSome with Some calls the block only once`() {
        val block = spyk({ _: String -> })

        expectThat(Some("ciao").ifSome(block))
            .isEqualTo(Unit)
        verify(exactly = 1) { block.invoke(eq("ciao")) }
    }

    @Test
    fun `calling runSome with None runs block only once and returns a new copy of Some`() {
        val block = spyk({ _: String -> })
        val input = Some("any")

        expectThat(input.runSome(block))
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 1) { block.invoke(eq("any")) }
    }

    @Test
    fun `mapping a Some calls transformation only once and returns a new Some`() {
        val someClosure = spyk({ _: String -> 1 })
        val input = Some("Any")

        expectThat(input.map(someClosure))
            .isEqualTo(Some(1))
        verify(exactly = 1) { someClosure.invoke(eq("Any")) }
    }

    @Test
    fun `flatmapping a Some calls transformation only once and returns a new Some`() {
        val someClosure = spyk({ y: Int -> Some(y.toString()) })
        val input = Some(120)

        expectThat(input.flatmap(someClosure))
            .isEqualTo(Some("120"))
        verify(exactly = 1) { someClosure.invoke(eq(120)) }
    }

    @Test
    fun `flatmapping a Some calls transformation only once and returns a None if transformation returns None`() {
        val someClosure = spyk({ _: Int -> None<Nothing>() })
        val input = Some(120)

        expectThat(input.flatmap(someClosure))
            .isA<None<Nothing>>()
        verify(exactly = 1) { someClosure.invoke(eq(120)) }
    }

    @Test
    fun `filtering a Some calls filter once`() {
        val filter = spyk({ _: String -> false })
        val input = Some("")

        input.filter(filter)
        verify(exactly = 1) { filter.invoke(eq("")) }
    }

    @Test
    fun `filtering a Some with false returns a None`() {
        expectThat(Some("body toucha ma spaghett").filter { false })
            .isA<None<String>>()
    }

    @Test
    fun `filtering a Some with true returns a new copy of Some`() {
        val input = Some("body toucha ma spaghett")
        expectThat(input.filter { true })
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Some or other = new copy of Some`() {
        val other = Some("value")
        val input = Some("")

        expectThat(input or other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Lazy or-ing a Some never calls the producer once and returns a new copy of Some`() {
        val producer = spyk({ Some("value") })
        val input = Some("")

        expectThat(input or producer)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)

        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `Some xor None = copy of Some`() {
        val other = None<String>()
        val input = Some("")

        expectThat(input xor other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
    }

    @Test
    fun `Some xor Some = None`() {
        val other = Some("abc")
        val input = Some("")

        expectThat(input xor other)
            .isA<None<String>>()
    }

    @Test
    fun `Lazy Some xor None = copy of Some`() {
        val other = spyk({ None<String>() })
        val input = Some("")

        expectThat(input xor other)
            .isNotSameInstanceAs(input)
            .isEqualTo(input)
        verify(exactly = 1) { other.invoke() }
    }

    @Test
    fun `Lazy Some xor Some = None`() {
        val other = spyk({ Some("abc") })
        val input = Some("")

        expectThat(input xor other)
            .isA<None<String>>()
        verify(exactly = 1) { other.invoke() }
    }

    @Test
    fun `Some and None = None`() {
        val other = None<String>()
        val input = Some("")

        expectThat(input and other)
            .isA<None<String>>()
    }

    @Test
    fun `Some and Some = second Some`() {
        val other = Some("abc")
        val input = Some("")

        expectThat(input and other)
            .isEqualTo(other)
    }

    @Test
    fun `Lazy and-ing a Some calls the producer once and returns that Some`() {
        val output = Some("value")
        val producer = spyk({ output })
        val input = Some("toucha")

        expectThat(input and producer)
            .isSameInstanceAs(output)
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `Lazy and-ing a None calls the producer once and returns another None`() {
        val output = None<String>()
        val producer = spyk({ output })
        val input = Some("toucha")

        expectThat(input and producer)
            .isNotSameInstanceAs(output)
            .isA<None<String>>()
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `And-ing a Some with combiner returns a new Some by calling the combiner`() {
        val other = Some("value")
        val combiner = spyk(String::plus)
        val input = Some("10")

        expectThat(input.and(other, combiner))
            .isEqualTo(Some("10value"))
        verify(exactly = 1) { combiner.invoke(eq("10"), eq("value")) }
    }

    @Test
    fun `And-ing a None with combiner returns a None and avoids calling the combiner`() {
        val other = None<String>()
        val combiner = spyk(String::plus)
        val input = Some("10")

        expectThat(input.and(other, combiner))
            .isNotSameInstanceAs(other)
            .isA<None<String>>()
        verify(exactly = 0) { combiner.invoke(any(), any()) }
    }

    @Test
    fun `Lazy and-ing a Some with combiner returns a new Some by calling the combiner`() {
        val other = Some("value")
        val combiner = spyk(String::plus)
        val producer = spyk({ other })
        val input = Some("10")

        expectThat(input.and(producer, combiner))
            .isEqualTo(Some("10value"))
        verify(exactly = 1) { combiner.invoke(eq("10"), eq("value")) }
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `Lazy and-ing a None with combiner returns a None and avoids calling the combiner`() {
        val other = None<String>()
        val combiner = spyk(String::plus)
        val producer = spyk({ other })
        val input = Some("10")

        expectThat(input.and(producer, combiner))
            .isNotSameInstanceAs(other)
            .isA<None<String>>()
        verify(exactly = 0) { combiner.invoke(any(), any()) }
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `Some zip Some = paired Some`() {
        expectThat(Some("8") zip Some(7))
            .isEqualTo(Some("8" to 7))
    }

    @Test
    fun `Some zip None = None`() {
        expectThat(Some("a") zip None<Nothing>())
            .isA<None<Pair<String, Nothing>>>()
    }

    @Test
    fun `Some is transformed as singleton`() {
        expectThat(Some("a").asList())
            .isEqualTo(listOf("a"))
    }

    @Test
    fun `Some is transformed as a value`() {
        expectThat(Some(6).asNullable())
            .isEqualTo(6)
            .isA<Int?>()
    }
}
