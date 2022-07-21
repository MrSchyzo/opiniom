package com.mrschyzo.opiniom.types

class ResultUnwrapException(val value: Any) : RuntimeException(message = "Unsuccessfully unwrapping a result : $value")

sealed class Result<Left: Any, Right: Any> {
    companion object {
        inline fun <Left: Any> from(block: () -> Left): Result<Left, Throwable> =
            try {
                Ok(block())
            } catch (e: Throwable) {
                Err(e)
            }

        fun <Left: Any> from(result: kotlin.Result<Left>): Result<Left, Throwable> =
            result.fold(onSuccess = ::Ok, onFailure = ::Err)

        fun <Left: Any, Right: Throwable> Result<Left, Right>.asResult(): kotlin.Result<Left> =
            this.match(ok = { kotlin.Result.success(it) }, err = { kotlin.Result.failure(it) })
    }

    abstract fun unwrap(): Left

    abstract fun <Left2: Any> mapOk(transform: (Left) -> Left2): Result<Left2, Right>
    abstract fun <Left2: Any> flatmapOk(transform: (Left) -> Result<Left2, Right>): Result<Left2, Right>

    abstract fun <Right2: Any> mapErr(transform: (Right) -> Right2): Result<Left, Right2>
    abstract fun <Right2: Any> flatmapErr(transform: (Right) -> Result<Left, Right2>): Result<Left, Right2>

    abstract fun <U: Any> match(ok: (Left) -> U, err: (Right) -> U): U

    abstract fun collapseError(): kotlin.Result<Left>
    abstract fun asMaybe(): Maybe<Left>
    abstract fun asNullable(): Left?
}

data class Ok<Left: Any, Right: Any>(val value: Left): Result<Left, Right>()
data class Err<Left: Any, Right: Any>(val value: Right): Result<Left, Right>()
