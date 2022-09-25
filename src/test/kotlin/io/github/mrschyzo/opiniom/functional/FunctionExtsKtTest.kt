package io.github.mrschyzo.opiniom.functional

import io.github.mrschyzo.opiniom.types.None
import io.github.mrschyzo.opiniom.types.Some
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class FunctionExtsKtTest {

    @Test
    fun `composition works as expected`() {
        val asList = { x: Int -> (1..x).toList() }
        val commaJoin = { x: List<Int> -> x.joinToString(",") }

        val composition = asList andThen commaJoin

        expectThat(composition(5))
            .isEqualTo("1,2,3,4,5")
    }

    @Test
    fun `identity returns the SAME reference object`() {
        expectThat(identity(5))
            .isSameInstanceAs(5)
    }

    @Test
    fun `partial application works as expected`() {
        val applied = String::plus withFixed "prefix"

        expectThat(applied("_postfix"))
            .isEqualTo("prefix_postfix")
    }

    @Test
    fun `flipped function works as expected`() {
        val flipped = String::plus.flip()

        expectThat(flipped("_postfix", "prefix"))
            .isEqualTo("prefix_postfix")
    }

    @Test
    fun `function if false = None`() {
        val foo = Int::toString `if` false

        expectThat(foo(10))
            .isA<None<String>>()
    }

    @Test
    fun `function if true = Some(result)`() {
        val foo = Int::toString `if` true

        expectThat(foo(50))
            .isEqualTo(Some("50"))
    }

    @Test
    fun `function if lazy-false = None`() {
        val foo = Int::toString `if` { x: Int -> x < 0 }

        expectThat(foo(10))
            .isA<None<String>>()
    }

    @Test
    fun `function if lazy-true = Some(result)`() {
        val foo = Int::toString `if` { x: Int -> x < 0 }

        expectThat(foo(-1))
            .isEqualTo(Some("-1"))
    }

    @Test
    fun `function onlyIf false = identity`() {
        val foo = { x: Int -> x + 1 } onlyIf false

        expectThat(foo(10))
            .isEqualTo(10)
    }

    @Test
    fun `function onlyIf true = function`() {
        val foo = { x: Int -> x + 1 } onlyIf true

        expectThat(foo(50))
            .isEqualTo(51)
    }

    @Test
    fun `function onlyIf lazy-false = identity`() {
        val foo = { x: Int -> -x } onlyIf { x: Int -> x < 0 }

        expectThat(foo(10))
            .isEqualTo(10)
    }

    @Test
    fun `function onlyIf lazy-true = function`() {
        val foo = { x: Int -> -x } onlyIf { x: Int -> x < 0 }

        expectThat(foo(-1))
            .isEqualTo(1)
    }

    @Test
    fun `function onlyUnless false = function`() {
        val foo = { x: Int -> x + 1 } onlyUnless false

        expectThat(foo(10))
            .isEqualTo(11)
    }

    @Test
    fun `function onlyUnless true = identity`() {
        val foo = { x: Int -> x + 1 } onlyUnless true

        expectThat(foo(50))
            .isEqualTo(50)
    }

    @Test
    fun `function onlyUnless lazy-false = function`() {
        val foo = { x: Int -> -x } onlyUnless { x: Int -> x < 0 }

        expectThat(foo(10))
            .isEqualTo(-10)
    }

    @Test
    fun `function onlyUnless lazy-true = identity`() {
        val foo = { x: Int -> -x } onlyUnless { x: Int -> x < 0 }

        expectThat(foo(-1))
            .isEqualTo(-1)
    }

    @Test
    fun `just returns constant functions`() {
        val foo = just<Any, _>(22)

        expectThat(foo(-1))
            .isEqualTo(22)
    }

    @Test
    fun `else unwraps if result is already some`() {
        val foo = just<Any, _>(Some(23))
        val coerce = foo `else` just(30)

        expectThat(coerce(Any()))
            .isEqualTo(23)
    }

    @Test
    fun `else coerce value if result is already some`() {
        val foo = just<Any, _>(None<Int>())
        val coerce = foo `else` just(22)

        expectThat(coerce(listOf<Any>()))
            .isEqualTo(22)
    }

    @Test
    fun `limited point-free is now possible`() {
        val append = (String::plus).flip()
        val prepend = (String::plus)

        val wrapIntoParentheses = (prepend withFixed "(") andThen (append withFixed ")")

        expectThat(wrapIntoParentheses("1,2,3"))
            .isEqualTo("(1,2,3)")
    }
}
