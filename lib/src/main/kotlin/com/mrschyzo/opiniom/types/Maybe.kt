package com.mrschyzo.opiniom.types

class UnwrapException(message: String = "Unwrapping failed"): RuntimeException(message)

sealed class Maybe<T: Any> {
    companion object {
        fun <T: Any> from(v: T?): Maybe<T> =
            v?.let(::Some) ?: None()

        fun <Inner: Any, T: Maybe<Inner>> Maybe<T>.flatten(): Maybe<Inner> =
            this.flatmap { it }
    }

    abstract fun unwrap(): T
    abstract fun <U: Any> orError(error: U): Result<T, U>
    abstract fun <U: Any> orError(error: () -> U): Result<T, U>
    abstract fun <U: Throwable> orElseThrow(ex: U): T
    abstract fun <U: Throwable> orElseThrow(ex: () -> U): T

    abstract fun orElse(fallback: T): T
    abstract fun orElse(fallback: () -> T): T

    abstract fun isSome(): Boolean
    fun isNone(): Boolean = !isSome()

    abstract fun <U: Any> match(some: (T) -> U, none: () -> U): U

    abstract fun ifSome(block: (T) -> Unit)
    abstract fun runSome(block: (T) -> Unit): Maybe<T>

    abstract fun ifNone(block: () -> Unit)
    abstract fun runNone(block: () -> Unit): Maybe<T>

    abstract fun <U: Any> map(transform: (T) -> U): Maybe<U>
    abstract fun <U: Any> flatmap(transform: (T) -> Maybe<U>): Maybe<U>

    abstract fun filter(filter: (T) -> Boolean): Maybe<T>

    abstract fun or(other: Maybe<T>): Maybe<T>
    abstract fun or(other: () -> Maybe<T>): Maybe<T>

    abstract fun xor(other: Maybe<T>): Maybe<T>
    abstract fun xor(other: () -> Maybe<T>): Maybe<T>

    abstract fun and(other: Maybe<T>): Maybe<T>
    abstract fun and(other: () -> Maybe<T>): Maybe<T>
    abstract fun and(other: Maybe<T>, combine: (T, T) -> T): Maybe<T>
    abstract fun and(other: () -> Maybe<T>, combine: (T, T) -> T): Maybe<T>

    abstract fun <U: Any> zip(other: Maybe<U>): Maybe<Pair<T, U>>

    abstract fun asList(): List<T>
    abstract fun asNullable(): T?
}


@Suppress("OVERRIDE_BY_INLINE")
class None<T: Any>: Maybe<T>() {
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

@Suppress("OVERRIDE_BY_INLINE")
data class Some<T: Any>(
    @PublishedApi internal val value: T
): Maybe<T>() {
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

    override fun and(other: () -> Maybe<T>): Maybe<T> = other().or(Some(value))

    override fun and(other: Maybe<T>): Maybe<T> = other.or(Some(value))

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
        other.flatmap<T> { None() }.or(Some(value))

    override inline fun xor(other: () -> Maybe<T>): Maybe<T> =
        other().flatmap<T> { None() }.or(Some(value))

    override inline fun and(other: Maybe<T>, crossinline combine: (T, T) -> T): Maybe<T> =
        other.map { combine(value, it) }

    override inline fun and(other: () -> Maybe<T>, crossinline combine: (T, T) -> T): Maybe<T> =
        other().map { combine(value, it) }

    override fun <U : Any> zip(other: Maybe<U>): Maybe<Pair<T, U>> =
        other.map { value to it }

    override fun asList(): List<T> = listOf(value)

    override fun asNullable(): T = value
}
