package de.jensklingenberg.ktorfit.converter.builtin

sealed class Response<T> {
    data class Success<T>(val data: T) : Response<T>()
    object Error : Response<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(ex: Throwable) = Error
    }
}
