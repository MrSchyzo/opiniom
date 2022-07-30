package io.github.mrschyzo.opiniom.types

/**
 * Result unwrap exception
 *
 * @property value
 * @constructor Create empty Result unwrap exception
 */
data class ResultUnwrapException(val value: Any) : RuntimeException("Unsuccessfully unwrapping a result: $value")

/**
 * `Either`ish flavor of [kotlin.Result]
 *
 * Its variants are [Ok] and [Err]
 *
 * @param Left
 * @param Right
 * @constructor Create empty Result
 */
@Suppress("TooManyFunctions")
sealed class Result<Left : Any, Right : Any> {
    companion object {
        /**
         * The equivalent to [kotlin.runCatching]
         */
        @JvmStatic
        @Suppress("TooGenericExceptionCaught")
        inline fun <Left : Any> from(block: () -> Left): Result<Left, Throwable> =
            try {
                Ok(block())
            } catch (e: Throwable) {
                Err(e)
            }

        /**
         * Converts a [kotlin.Result] into a [Result]
         */
        @JvmStatic
        fun <Left : Any> from(result: kotlin.Result<Left>): Result<Left, Throwable> =
            result.fold(onSuccess = ::Ok, onFailure = ::Err)

        /**
         * Extension method for [kotlin.Result] that is equivalent to [Result.from]
         */
        @JvmStatic
        fun <Left : Any> kotlin.Result<Left>.eitherify(): Result<Left, Throwable> =
            from(this)

        /**
         * Similar to [Result.asKtResult], but it avoids wrapping any [Right] into a [ResultUnwrapException]
         */
        @JvmStatic
        fun <Left : Any, Right : Throwable> Result<Left, Right>.asResult(): kotlin.Result<Left> =
            this.match(
                ok = { kotlin.Result.success(it) },
                err = { kotlin.Result.failure(it) }
            )
    }

    /**
     * Unsafe unwrap, can throw exceptions
     *
     * @return [Left] if [Ok]
     * @throws [ResultUnwrapException] if [Err]
     */
    abstract fun unwrap(): Left

    /**
     * Checks whether this [Result] is [Ok]
     */
    abstract fun isOk(): Boolean

    /**
     * Checks whether this [Result] is [Err]
     *
     * @return basically the opposite of [isOk]
     */
    fun isErr(): Boolean = !isOk()

    /**
     * Consumes the [Result] by executing [block] only if this is [Ok]
     *
     * @param block action to execute, consumes a [Left]
     */
    abstract fun ifOk(block: (Left) -> Unit)

    /**
     * Consumes the [Result] by executing [block] only if this is [Err]
     *
     * @param block action to execute, consumes a [Right]
     */
    abstract fun ifErr(block: (Right) -> Unit)

    /**
     * Similar to [ifOk], but it returns a copy of this [Result]
     */
    abstract fun runOk(block: (Left) -> Unit): Result<Left, Right>

    /**
     * Similar to [ifErr], but it returns a copy of this [Result]
     */
    abstract fun runErr(block: (Right) -> Unit): Result<Left, Right>

    /**
     * Transforms the contained [Left], if this is [Ok]
     *
     * @param Left2 type of the result of [transform]
     * @param transform transformation that consumes [Left]
     * @return A transformed [Ok], if [Ok]; [Err] otherwise
     */
    abstract fun <Left2 : Any> mapOk(transform: (Left) -> Left2): Result<Left2, Right>

    /**
     * Similar to [mapOk], but it avoids returning nesting [Result]
     *
     * @param Left2 type of the contained result of [transform]
     * @param transform transformation that consumes [Left]
     * @return A transformed [Ok], if this is [Ok]; [Err] if this is [Err], or [transform] returns [Err]
     */
    abstract fun <Left2 : Any> flatmapOk(transform: (Left) -> Result<Left2, Right>): Result<Left2, Right>

    /**
     * Analogous to [mapOk], but for [Err] value
     */
    abstract fun <Right2 : Any> mapErr(transform: (Right) -> Right2): Result<Left, Right2>

    /**
     * Analogous to [flatmapOk], but for [Err] value
     */
    abstract fun <Right2 : Any> flatmapErr(transform: (Right) -> Result<Left, Right2>): Result<Left, Right2>

    /**
     * `and` version for the [Result]
     *
     * @param result
     * @return [result], if this is [Ok], [Err] otherwise
     */
    abstract infix fun and(result: Result<Left, Right>): Result<Left, Right>

    /**
     * Lazy version of [and]
     */
    abstract infix fun and(result: () -> Result<Left, Right>): Result<Left, Right>

    /**
     * Keeps the first [Ok] between this and [result], [Err] otherwise
     *
     * @param result
     * @return one of the [Ok] results: this instance has priority over [result]; [Err] otherwise
     */
    abstract infix fun or(result: Result<Left, Right>): Result<Left, Right>

    /**
     * Lazy version of [or]
     */
    abstract infix fun or(result: () -> Result<Left, Right>): Result<Left, Right>

    /**
     * Match
     *
     * @param U
     * @param ok
     * @param err
     * @receiver
     * @receiver
     * @return
     */
    abstract fun <U : Any> match(ok: (Left) -> U, err: (Right) -> U): U

    /**
     * @return a [kotlin.Result.success] if [Ok], [kotlin.Result.failure] with [ResultUnwrapException] if [Err]
     */
    abstract fun asKtResult(): kotlin.Result<Left>

    /**
     * @return [Some] if [Ok], [None] if [Err]
     */
    abstract fun extractOk(): Maybe<Left>

    /**
     * @return [Some] if [Err], [None] if [Ok]
     */
    abstract fun extractErr(): Maybe<Right>
}

/**
 * [Ok] represents a [Result] that contains only a [Left] instance
 *
 * @param Left value held
 * @param Right irrelevant
 * @property left value held
 */
@Suppress("OVERRIDE_BY_INLINE", "TooManyFunctions")
data class Ok<Left : Any, Right : Any>(val left: Left) : Result<Left, Right>() {
    override fun unwrap(): Left = left

    override fun isOk(): Boolean = true

    override inline fun ifOk(block: (Left) -> Unit) = block(left)

    override fun ifErr(block: (Right) -> Unit) = Unit

    override fun runOk(block: (Left) -> Unit): Result<Left, Right> {
        block(left)
        return Ok(left)
    }

    override fun runErr(block: (Right) -> Unit): Result<Left, Right> = Ok(left)

    override inline fun <Left2 : Any> mapOk(transform: (Left) -> Left2): Result<Left2, Right> =
        Ok(transform(left))

    override fun <Left2 : Any> flatmapOk(transform: (Left) -> Result<Left2, Right>): Result<Left2, Right> =
        transform(left)

    override fun <Right2 : Any> mapErr(transform: (Right) -> Right2): Result<Left, Right2> = Ok(left)

    override fun <Right2 : Any> flatmapErr(transform: (Right) -> Result<Left, Right2>): Result<Left, Right2> = Ok(left)

    override fun and(result: Result<Left, Right>): Result<Left, Right> = result

    override inline fun and(result: () -> Result<Left, Right>): Result<Left, Right> = result()

    override fun or(result: Result<Left, Right>): Result<Left, Right> = Ok(left)

    override fun or(result: () -> Result<Left, Right>): Result<Left, Right> = Ok(left)

    override fun <U : Any> match(ok: (Left) -> U, err: (Right) -> U): U = ok(left)

    override fun asKtResult(): kotlin.Result<Left> = kotlin.Result.success(left)

    override fun extractOk(): Maybe<Left> = Some(left)

    override fun extractErr(): Maybe<Right> = None()
}

/**
 * [Err] represents a [Result] that contains only a [Right] instance
 *
 * @param Left irrelevant
 * @param Right value held
 * @property right value held
 */
@Suppress("OVERRIDE_BY_INLINE", "TooManyFunctions")
data class Err<Left : Any, Right : Any>(val right: Right) : Result<Left, Right>() {
    override fun unwrap(): Left =
        throw ResultUnwrapException(right)

    override fun isOk(): Boolean = false

    override fun ifOk(block: (Left) -> Unit) = Unit

    override inline fun ifErr(block: (Right) -> Unit) = block(right)

    override fun runOk(block: (Left) -> Unit): Result<Left, Right> = Err(right)

    override inline fun runErr(block: (Right) -> Unit): Result<Left, Right> {
        block(right)
        return Err(right)
    }

    override fun <Left2 : Any> mapOk(transform: (Left) -> Left2): Result<Left2, Right> = Err(right)

    override fun <Left2 : Any> flatmapOk(transform: (Left) -> Result<Left2, Right>): Result<Left2, Right> = Err(right)

    override inline fun <Right2 : Any> mapErr(transform: (Right) -> Right2): Result<Left, Right2> =
        Err(transform(right))

    override inline fun <Right2 : Any> flatmapErr(transform: (Right) -> Result<Left, Right2>): Result<Left, Right2> =
        transform(right)

    override fun and(result: Result<Left, Right>): Result<Left, Right> = Err(right)

    override fun and(result: () -> Result<Left, Right>): Result<Left, Right> = Err(right)

    override fun or(result: Result<Left, Right>): Result<Left, Right> = result

    override inline fun or(result: () -> Result<Left, Right>): Result<Left, Right> = result()

    override inline fun <U : Any> match(ok: (Left) -> U, err: (Right) -> U): U = err(right)

    override fun asKtResult(): kotlin.Result<Left> = kotlin.Result.failure(ResultUnwrapException(right))

    override fun extractOk(): Maybe<Left> = None()

    override fun extractErr(): Maybe<Right> = Some(right)
}
