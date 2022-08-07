package io.github.mrschyzo.opiniom.functional

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
@Suppress("FunctionNaming")
infix fun <O> ((O) -> O).`if`(condition: Boolean): (O) -> O =
    if (condition)
        this
    else
        ::identity

/**
 * Lazy version of [if]
 *
 * @param O
 * @param condition
 * @return
 */
@Suppress("FunctionNaming")
inline infix fun <O> ((O) -> O).`if`(crossinline condition: (O) -> Boolean): (O) -> O = { value: O ->
    (this `if` condition(value))(value)
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
infix fun <O> ((O) -> O).unless(condition: Boolean): (O) -> O =
    this `if` (!condition)

/**
 * Lazy version of [unless]
 *
 * @param O
 * @param condition
 * @return
 */
inline infix fun <O> ((O) -> O).unless(crossinline condition: (O) -> Boolean): (O) -> O =
    this `if` (condition andThen Boolean::not)
