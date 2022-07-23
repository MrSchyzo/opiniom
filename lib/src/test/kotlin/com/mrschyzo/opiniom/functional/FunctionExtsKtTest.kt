package com.mrschyzo.opiniom.functional

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class FunctionExtsKtTest {

    @Test
    fun `composition works as expected`() {
        val asList = {x: Int -> (1 .. x).toList()}
        val commaJoin = {x: List<Int> -> x.joinToString(",")}

        val composition = asList andThen commaJoin

        expectThat(composition(5))
            .isEqualTo("1,2,3,4,5")
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
    fun `limited point-free is now possible`() {
        val append = (String::plus).flip()
        val prepend = (String::plus)

        val wrapIntoParentheses = (prepend withFixed "(") andThen (append withFixed ")")

        expectThat(wrapIntoParentheses("1,2,3"))
            .isEqualTo("(1,2,3)")
    }
}
