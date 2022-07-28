package io.github.mrschyzo.opiniom.types

/**
 * Used to signal an unsafe unwrapping of a [Maybe]
 *
 * @param message
 */
class UnwrapException(message: String = "Unwrapping failed") : RuntimeException(message)

/**
 * [Maybe] class defines a Maybe monad along with its operations
 *
 * Its variants are [Some] and [None]
 *
 * @param T Type of the value potentially contained by [Maybe]
 */
@Suppress("TooManyFunctions")
sealed class Maybe<T : Any> {
    companion object {

        /**
         * Converts a nullable type into a [Maybe] instance
         *
         * @param T Maybe's content type
         * @param v Maybe's content
         * @return [None] if [v] is null, [Some] instance containing [v] otherwise
         */
        @JvmStatic
        fun <T : Any> from(v: T?): Maybe<T> =
            v?.let(::Some) ?: None()

        /**
         * It flattens the levels of [Maybe]
         *
         * Used to transform `Maybe<Maybe<X>>` into a `Maybe<X>`
         *
         * @param Inner effective content's type
         * @return A flattened [Maybe]
         */
        @JvmStatic
        fun <Inner : Any> Maybe<Maybe<Inner>>.flatten(): Maybe<Inner> =
            this.flatmap { it }
    }

    /**
     * Unsafely extracts the value
     *
     * @return [T] The contained value, if [Some]
     * @throws [UnwrapException] if `this` is [None]
     */
    abstract fun unwrap(): T

    /**
     * Puts the value into a [Result]
     *
     * [Ok] = [Some]; [Err] = [None]
     *
     * @param U type of the error variant
     * @param error error variant
     * @return [Ok] with the contained value if [Some], [Err] with the [error] if [None]
     */
    abstract fun <U : Any> orError(error: U): Result<T, U>

    /**
     * Lazy version of [orError]
     */
    abstract fun <U : Any> orError(error: () -> U): Result<T, U>

    /**
     * Similar to [unwrap], but you decide what [Throwable] is to be thrown in case of [None]
     *
     * @param U type of the exception to throw, if [None]
     * @param ex exception to throw, if [None]
     * @return the value if [Some], throws [error] if [None]
     */
    abstract fun <U : Throwable> orElseThrow(ex: U): T

    /**
     * Lazy version of [orElseThrow]
     */
    abstract fun <U : Throwable> orElseThrow(ex: () -> U): T

    /**
     * Returns the value or falls back to the given value
     *
     * @param fallback value to be return, if [None]
     * @return like [unwrap], if [Some]; [fallback] if [None]
     */
    abstract fun orElse(fallback: T): T

    /**
     * Lazy version of [orElse]
     */
    abstract fun orElse(fallback: () -> T): T

    /**
     * Checks whether this instance is [Some]
     *
     * @return `true` if [Some]
     */
    abstract fun isSome(): Boolean

    /**
     * Checks whether this instance is [None]
     *
     * @return the opposite of [isSome]
     */
    fun isNone(): Boolean = !isSome()

    /**
     * Rough implementation of a pattern matching, with the possibility to return an instance of something [U]
     *
     * @param U value of the conversion
     * @param some conversion of the value, if [Some]
     * @param none generation of a value, if [None]
     * @return an instance of [U]
     */
    abstract fun <U : Any> match(some: (T) -> U, none: () -> U): U

    /**
     * Runs [block] if this is [Some], consuming the [Maybe]
     *
     * @param block consumer of the contained value
     */
    abstract fun ifSome(block: (T) -> Unit)

    /**
     * Similar to [ifSome], but it returns a new [Maybe] copy at the end
     */
    abstract fun runSome(block: (T) -> Unit): Maybe<T>

    /**
     * Runs [block] if this is [None], consuming the [Maybe]
     *
     * @param block an action
     * @receiver
     */
    abstract fun ifNone(block: () -> Unit)

    /**
     * Similar to [ifNone], but it returns a new [Maybe] copy at the end
     */
    abstract fun runNone(block: () -> Unit): Maybe<T>

    /**
     * Transforms the contained value
     *
     * @param U type of the transformed value
     * @param transform conversion function
     * @return [Some] with a transformed value, [None] if no value is contained
     */
    abstract fun <U : Any> map(transform: (T) -> U): Maybe<U>

    /**
     * Similar to [map]
     *
     * Can be roughly seen as a [map] + [flatten]
     */
    abstract fun <U : Any> flatmap(transform: (T) -> Maybe<U>): Maybe<U>

    /**
     * Returns a copy of [Some] if the contained value respects the [filter], [None] otherwise
     *
     * @param filter filter function
     * @return a copy of [Some] if the contained value respects the [filter], [None] otherwise
     */
    abstract fun filter(filter: (T) -> Boolean): Maybe<T>

    /**
     * Returns a copy of this [Some], otherwise returns the [other] [Maybe]
     * @param other used as a "fallback"
     * @return a copy of this [Some], otherwise returns the [other] [Maybe]
     */
    abstract infix fun or(other: Maybe<T>): Maybe<T>

    /**
     * Lazy version of [or]
     */
    abstract infix fun or(other: () -> Maybe<T>): Maybe<T>

    /**
     * Returns the only [Some] between this and [other], [None] otherwise
     *
     * A `xor` version for [Maybe]
     *
     * @param other the other [Maybe] to match against
     * @return the only [Some] between this and [other], [None] otherwise
     */
    abstract infix fun xor(other: Maybe<T>): Maybe<T>

    /**
     * Lazy version of [xor]
     */
    abstract infix fun xor(other: () -> Maybe<T>): Maybe<T>

    /**
     * Returns [other] if [Some], [None] otherwise
     */
    abstract infix fun and(other: Maybe<T>): Maybe<T>

    /**
     * Lazy version of [and]
     */
    abstract infix fun and(other: () -> Maybe<T>): Maybe<T>

    /**
     * Custom version of [and], "collision" is explicitly handled by [combine]
     */
    abstract fun and(other: Maybe<T>, combine: (T, T) -> T): Maybe<T>

    /**
     * Custom lazy version of [and], "collision" is explicitly handled by [combine]
     */
    abstract fun and(other: () -> Maybe<T>, combine: (T, T) -> T): Maybe<T>

    /**
     * If [other] and this are [Some], returns a new [Maybe] that contains a [Pair] of both values
     *
     * @param other the other [Maybe] to combine with this
     * @return returns a new [Maybe] that contains a [Pair] of both values, [None] otherwise
     */
    abstract infix fun <U : Any> zip(other: Maybe<U>): Maybe<Pair<T, U>>

    /**
     * @return Singleton if [Some], empty if [None]
     */
    abstract fun asList(): List<T>

    /**
     * Basically, inverse of [from]
     */
    abstract fun asNullable(): T?
}

/**
 * [None] represents a [Maybe] without any value
 */
@Suppress("OVERRIDE_BY_INLINE", "TooManyFunctions")
class None<T : Any> : Maybe<T>() {

    override fun unwrap(): T = throw UnwrapException("Unwrapping None")

    override fun <U : Any> orError(error: U): Result<T, U> = Err(error)

    override inline fun <U : Any> orError(error: () -> U): Result<T, U> = Err(error())

    override fun <U : Throwable> orElseThrow(ex: U): T = throw ex

    override inline fun <U : Throwable> orElseThrow(ex: () -> U): T = throw ex()

    override fun orElse(fallback: T): T = fallback

    override inline fun orElse(fallback: () -> T): T = fallback()

    override fun isSome(): Boolean = false

    override inline fun <U : Any> match(some: (T) -> U, none: () -> U): U = none()

    override fun ifSome(block: (T) -> Unit) = Unit

    override fun runSome(block: (T) -> Unit): Maybe<T> = None()

    override inline fun ifNone(block: () -> Unit) = block()

    override inline fun runNone(block: () -> Unit): Maybe<T> {
        block()
        return None()
    }

    override fun <U : Any> map(transform: (T) -> U): Maybe<U> = None()

    override fun <U : Any> flatmap(transform: (T) -> Maybe<U>): Maybe<U> = None()

    override fun filter(filter: (T) -> Boolean): Maybe<T> = None()

    override fun or(other: Maybe<T>): Maybe<T> = other

    override inline fun or(other: () -> Maybe<T>): Maybe<T> = other()

    override fun xor(other: Maybe<T>): Maybe<T> = other

    override inline fun xor(other: () -> Maybe<T>): Maybe<T> = other()

    override fun and(other: () -> Maybe<T>): Maybe<T> = None()

    override fun and(other: Maybe<T>): Maybe<T> = None()

    override fun and(other: Maybe<T>, combine: (T, T) -> T): Maybe<T> = None()

    override fun and(other: () -> Maybe<T>, combine: (T, T) -> T): Maybe<T> = None()

    override fun <U : Any> zip(other: Maybe<U>): Maybe<Pair<T, U>> = None()

    override fun asList(): List<T> = listOf()

    override fun asNullable(): T? = null
}

/**
 * [Some] represents a [Maybe] with a value
 */
@Suppress("OVERRIDE_BY_INLINE", "TooManyFunctions")
data class Some<T : Any>(
    @PublishedApi internal val value: T
) : Maybe<T>() {
    override fun unwrap(): T = value

    override fun <U : Any> orError(error: U): Result<T, U> =
        Ok(value)

    override fun <U : Any> orError(error: () -> U): Result<T, U> =
        Ok(value)

    override fun <U : Throwable> orElseThrow(ex: U): T = value

    override fun <U : Throwable> orElseThrow(ex: () -> U): T = value

    override fun orElse(fallback: T): T = value

    override fun orElse(fallback: () -> T): T = value

    override fun isSome(): Boolean = true

    override inline fun <U : Any> match(some: (T) -> U, none: () -> U): U =
        some(value)

    override inline fun ifSome(block: (T) -> Unit) = block(value)

    override inline fun runSome(block: (T) -> Unit): Maybe<T> {
        block(value)
        return Some(value)
    }

    override fun ifNone(block: () -> Unit) = Unit

    override fun and(other: () -> Maybe<T>): Maybe<T> =
        and(other())

    override fun and(other: Maybe<T>): Maybe<T> =
        other.match(
            some = { other },
            none = ::None
        )

    override fun runNone(block: () -> Unit): Maybe<T> = Some(value)

    override inline fun <U : Any> map(transform: (T) -> U): Maybe<U> =
        Some(transform(value))

    override inline fun <U : Any> flatmap(transform: (T) -> Maybe<U>): Maybe<U> =
        transform(value)

    override inline fun filter(filter: (T) -> Boolean): Maybe<T> =
        if (filter(value))
            Some(value)
        else
            None()

    override fun or(other: Maybe<T>): Maybe<T> = Some(value)

    override fun or(other: () -> Maybe<T>): Maybe<T> = Some(value)

    override fun xor(other: Maybe<T>): Maybe<T> =
        other.match(
            none = { Some(value) },
            some = { None() }
        )

    override inline fun xor(other: () -> Maybe<T>): Maybe<T> =
        other().match(
            none = { Some(value) },
            some = { None() }
        )

    override inline fun and(other: Maybe<T>, crossinline combine: (T, T) -> T): Maybe<T> =
        other.map { combine(value, it) }

    override inline fun and(other: () -> Maybe<T>, crossinline combine: (T, T) -> T): Maybe<T> =
        other().map { combine(value, it) }

    override fun <U : Any> zip(other: Maybe<U>): Maybe<Pair<T, U>> =
        other.map { value to it }

    override fun asList(): List<T> = listOf(value)

    override fun asNullable(): T = value
}
