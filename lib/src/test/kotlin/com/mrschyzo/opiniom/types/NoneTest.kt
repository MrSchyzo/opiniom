package com.mrschyzo.opiniom.types

import com.mrschyzo.opiniom.functional.andThen
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*

internal class NoneTest {

    @Test
    fun `unwrapping a None throws a UnwrapException`() {
        expectThrows<UnwrapException> { None<String>().unwrap() }
    }

    @Test
    fun `orError-ing a None returns the given Err`() {
        expectThat(None<Any>().orError(7))
            .isEqualTo(Err(7))
    }

    @Test
    fun `lazy onError-ing a None calls the producer exactly once`() {
        val producer = spyk({"ERROR"})
        val error = None<Nothing>().orError(producer)

        verify(exactly = 1) { producer.invoke() }
        expectThat(error).isEqualTo(Err("ERROR"))
    }

    @Test
    fun `eager orElseThrow-ing a None throws the given exception`() {
        val exception = OutOfMemoryError()

        expectThrows<OutOfMemoryError> { None<Any>().orElseThrow(exception) }
            .isEqualTo(exception)
    }

    @Test
    fun `lazy orElseThrow-ing a None calls the producer exactly once and throws the given exception`() {
        val exception = OutOfMemoryError()
        val producer = spyk({exception})

        expectThrows<OutOfMemoryError> { None<Nothing>().orElseThrow(producer) }
            .isEqualTo(exception)
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `orElse-ing a None returns the expected fallback value`() {
        expectThat(None<String>().orElse("Fallback"))
            .isEqualTo("Fallback")
    }

    @Test
    fun `lazy orElse-ing a None calls the producer only once and returns the expected fallback value`() {
        val producer = spyk({"fallback"})

        expectThat(None<String>().orElse(producer))
            .isEqualTo("fallback")
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `None is not a Some, DUH!`() {
        expectThat(None<String>())
            .assertThat("Returns false for isSome", None<String>::isSome andThen Boolean::not)
            .assertThat("Returns true for isNone", None<String>::isNone)
    }

    @Test
    fun `match-ing a None only calls once the 'none' closure, 'some' closure is not called at all`() {
        val noneClosure = spyk({"abcde"})
        val someClosure = spyk({y:String -> y})

        expectThat(None<String>().match(some = someClosure, none = noneClosure))
            .isEqualTo("abcde")
        verify(exactly = 1) { noneClosure.invoke() }
        verify(exactly = 0) { someClosure.invoke(any()) }
    }

    @Test
    fun `calling ifSome with None does nothing`() {
        val block = spyk({_:String -> })

        expectThat(None<String>().ifSome(block))
            .isEqualTo(Unit)
        verify(exactly = 0) { block.invoke(any()) }
    }

    @Test
    fun `calling runSome with None does nothing but returns a new None`() {
        val block = spyk({_:Any -> })
        val input = None<Any>()

        expectThat(input.runSome(block))
            .isNotEqualTo(input)
            .isA<None<Any>>()
        verify(exactly = 0) { block.invoke(any()) }
    }

    @Test
    fun `calling ifNone with None calls the block only once`() {
        val block = spyk({})

        expectThat(None<String>().ifNone(block))
            .isEqualTo(Unit)
        verify(exactly = 1) { block.invoke() }
    }

    @Test
    fun `calling runNone with None runs block only once and returns a new None`() {
        val block = spyk({})
        val input = None<Any>()

        expectThat(input.runNone(block))
            .isNotEqualTo(input)
            .isA<None<Any>>()
        verify(exactly = 1) { block.invoke() }
    }

    @Test
    fun `mapping a None does not call transformation but returns a new None`() {
        val someClosure = spyk({y:String -> y})
        val input = None<String>()

        expectThat(input.map(someClosure))
            .isNotEqualTo(input)
            .isA<None<String>>()
        verify(exactly = 0) { someClosure.invoke(any()) }
    }

    @Test
    fun `flatmapping a None does not call transformation but returns a new None`() {
        val someClosure = spyk({y:String -> Some(y)})
        val input = None<String>()

        expectThat(input.flatmap(someClosure))
            .isNotEqualTo(input)
            .isA<None<String>>()
        verify(exactly = 0) { someClosure.invoke(any()) }
    }

    @Test
    fun `filtring a None does not call filter but returns a new None`() {
        val filter = spyk({ y:String -> false})
        val input = None<String>()

        expectThat(input.filter(filter))
            .isNotEqualTo(input)
            .isA<None<String>>()
        verify(exactly = 0) { filter.invoke(any()) }
    }

    @Test
    fun `None or other = other`() {
        val other = Some("value")

        expectThat(None<String>() or other)
            .isEqualTo(other)
    }

    @Test
    fun `Lazy or-ing a None calls the producer once and returns its output`() {
        val producer = spyk({ Some("value") })

        expectThat(None<String>() or producer)
            .isEqualTo(Some("value"))
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `None xor other = other`() {
        val other = Some("value")

        expectThat(None<String>() xor other)
            .isEqualTo(other)
    }

    @Test
    fun `Lazy xor-ing a None calls the producer once and returns its output`() {
        val producer = spyk({ Some("value") })

        expectThat(None<String>() xor producer)
            .isEqualTo(Some("value"))
        verify(exactly = 1) { producer.invoke() }
    }

    @Test
    fun `None and other = a copy of None`() {
        val other = Some("value")
        val input = None<String>()

        expectThat(input and other)
            .isNotEqualTo(input)
            .isA<None<String>>()
    }

    @Test
    fun `Lazy and-ing a None never calls the producer and returns a new None`() {
        val producer = spyk({ Some("value") })
        val input = None<String>()

        expectThat(input and producer)
            .isNotEqualTo(input)
            .isA<None<String>>()
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `And-ing a None with combiner returns a new None without calling the combiner`() {
        val other = Some("value")
        val combiner = spyk({x:String,_:String->x})
        val input = None<String>()

        expectThat(input.and(other, combiner))
            .isNotEqualTo(input)
            .isA<None<String>>()
        verify(exactly = 0) { combiner.invoke(any(), any()) }
    }

    @Test
    fun `Lazy and-ing a None with combiner calls neither producer nor combiner and returns a new None`() {
        val producer = spyk({ Some("value") })
        val combiner = spyk({x:String,_:String->x})
        val input = None<String>()

        expectThat(input.and(producer, combiner))
            .isNotEqualTo(input)
            .isA<None<String>>()
        verify(exactly = 0) { combiner.invoke(any(), any()) }
        verify(exactly = 0) { producer.invoke() }
    }

    @Test
    fun `Zipping a None with a Some returns a new None`() {
        expectThat(None<String>() zip Some(7))
            .isA<None<Pair<String, Int>>>()
    }

    @Test
    fun `Zipping a None with a None returns a new None`() {
        expectThat(None<String>() zip None<Nothing>())
            .isA<None<Pair<String, Nothing>>>()
    }

    @Test
    fun `None is transformed as an empty list`() {
        expectThat(None<Any>().asList())
            .hasSize(0)
    }

    @Test
    fun `None is transformed as a null`() {
        expectThat(None<Any>().asNullable())
            .isEqualTo(null)
    }
}
