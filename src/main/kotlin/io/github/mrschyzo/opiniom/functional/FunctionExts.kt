@file:Suppress("TooManyFunctions")
package io.github.mrschyzo.opiniom.functional

import io.github.mrschyzo.opiniom.types.Maybe
import io.github.mrschyzo.opiniom.types.None
import io.github.mrschyzo.opiniom.types.Some

/**
 * Identity function
 *
 * @param T
 * @param t
 * @return
 */
fun <T> identity(t: T): T = t

/**
 * Function composition
 *
 * Usage: `(f andThen g)(x)`, that means `g(f(x))`
 *
 * @param A first function input
 * @param B second function input, first function output
 * @param C second function output
 * @param next second function
 * @receiver first function
 * @return
 */
inline infix fun <A, B, C> ((A) -> B).andThen(crossinline next: (B) -> C): (A) -> C = {
    next(this(it))
}

/**
 * Partially applies the first parameter of a 2-args function
 *
 * Usage: `(f apply value)(y)`, that means `f(value, y)`
 * @param A first input
 * @param B second input
 * @param C output
 * @param input value to fix against the partial application
 * @return partial application of the function with the first parameter applied
 */
infix fun <A, B, C> ((A, B) -> C).withFixed(input: A): (B) -> C = {
    this(input, it)
}

/**
 * Flips a 2-args function arguments
 *
 * Usage: `sum.flip()` means `sum(y,x) instead of sum(x,y)`
 *
 * @param A first input
 * @param B second input
 * @param C output
 * @return a function with the two input arguments flipped
 */
fun <A, B, C> ((A, B) -> C).flip(): (B, A) -> C = { b, a ->
    this(a, b)
}

/**
 * Conditional application of endomorphism
 *
 * Usage: `(foo if [condition])(x)`
 *
 * if the [condition] is true, return [this] function
 *
 * if the [condition] is false, return [identity] function reference
 *
 * @param O
 * @param condition
 * @return
 */
infix fun <O> ((O) -> O).onlyIf(condition: Boolean): (O) -> O =
    if (condition)
        this
    else
        ::identity

/**
 * Lazy version of [`if`]
 *
 * @param O
 * @param condition
 * @return
 */
inline infix fun <O> ((O) -> O).onlyIf(crossinline condition: (O) -> Boolean): (O) -> O = { value: O ->
    (this onlyIf condition(value))(value)
}

/**
 * Conditional application of endomorphism
 *
 * Usage: `(foo unless [condition])(x)`
 *
 * if the [condition] is false, return [this] function
 *
 * if the [condition] is true, return [identity] function reference
 *
 * @param O
 * @param condition
 * @return
 */
infix fun <O> ((O) -> O).onlyUnless(condition: Boolean): (O) -> O =
    this onlyIf (!condition)

/**
 * Lazy version of [onlyUnless]
 *
 * @param O
 * @param condition
 * @return
 */
inline infix fun <O> ((O) -> O).onlyUnless(crossinline condition: (O) -> Boolean): (O) -> O =
    this onlyIf (condition andThen Boolean::not)

/**
 * Applies [this] function if [condition] is true, else return a [None]
 *
 * @param condition Decides whether the function is applied or not
 * @return A function returning [this] function result wrapped into a [Some], a function returning [None] otherwise
 */
@Suppress("FunctionNaming")
infix fun <I : Any, O : Any> ((I) -> O).`if`(condition: Boolean): (I) -> Maybe<O> = {
    if (condition)
        Some(this(it))
    else
        None()
}

/**
 * A functional version of [if]
 *
 * @param condition Function to compute the condition on the fly before deciding whether applying [this] function
 * @return A function returning [this] function result wrapped into a [Some], a function returning [None] otherwise
 */
@Suppress("FunctionNaming")
inline infix fun <I : Any, O : Any> ((I) -> O).`if`(crossinline condition: (I) -> Boolean): (I) -> Maybe<O> = {
    (this `if` condition(it))(it)
}

/**
 * Coerces this function to return a value if its result is [None]
 *
 * @param alternative the coercitive function to apply to the original input
 * @return A function that applies [this] partial function,
 *         if [None] is returned, the result is [alternative] applied to [this] function input
 */
@Suppress("FunctionNaming")
inline infix fun <I : Any, O : Any> ((I) -> Maybe<O>).`else`(crossinline alternative: (I) -> O): (I) -> O = {
    this(it).orElse(alternative(it))
}

/**
 * Defines a constant function regardless of the input
 *
 * @param O the value to be returned in this constant function
 * @return a constant function with [output] as the result
 */
fun <I, O> just(output: O): (I) -> O = { output }
