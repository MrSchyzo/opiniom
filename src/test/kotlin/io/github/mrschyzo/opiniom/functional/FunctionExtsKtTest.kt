package io.github.mrschyzo.opiniom.functional

import org.junit.jupiter.api.Test
import strikt.api.expectThat
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
    fun `function if false = identity`() {
        val foo = { x: Int -> x + 1 } `if` false

        expectThat(foo(10))
            .isEqualTo(10)
    }

    @Test
    fun `function if true = function`() {
        val foo = { x: Int -> x + 1 } `if` true

        expectThat(foo(50))
            .isEqualTo(51)
    }

    @Test
    fun `function if lazy-false = identity`() {
        val foo = { x: Int -> -x } `if` { x: Int -> x < 0 }

        expectThat(foo(10))
            .isEqualTo(10)
    }

    @Test
    fun `function if lazy-true = function`() {
        val foo = { x: Int -> -x } `if` { x: Int -> x < 0 }

        expectThat(foo(-1))
            .isEqualTo(1)
    }

    @Test
    fun `function unless false = function`() {
        val foo = { x: Int -> x + 1 } unless false

        expectThat(foo(10))
            .isEqualTo(11)
    }

    @Test
    fun `function unless true = identity`() {
        val foo = { x: Int -> x + 1 } unless true

        expectThat(foo(50))
            .isEqualTo(50)
    }

    @Test
    fun `function unless lazy-false = function`() {
        val foo = { x: Int -> -x } unless { x: Int -> x < 0 }

        expectThat(foo(10))
            .isEqualTo(-10)
    }

    @Test
    fun `function unless lazy-true = identity`() {
        val foo = { x: Int -> -x } unless { x: Int -> x < 0 }

        expectThat(foo(-1))
            .isEqualTo(-1)
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
