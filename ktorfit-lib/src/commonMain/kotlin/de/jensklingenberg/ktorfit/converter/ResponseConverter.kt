package de.jensklingenberg.ktorfit.converter

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.statement.*
import io.ktor.util.reflect.*

/**
 * Implement this to support wrapping for custom types
 * in suspend functions e.g.
 * suspend fun test() : MyCustomType<String>
 */
interface ResponseConverter : CoreResponseConverter {

    /**
     * @param requestFunction a suspend function that will return a typeInfo of Ktor's requested type and the [HttpResponse]
     * @param returnTypeName is the qualified name of the outer type of
     * the return type. e.g. for Flow<String> it will be kotlinx.coroutines.flow.Flow
     * @return the wrapped response
     */
    fun <PRequest : Any?> convertResponse(
        returnTypeName: String,
        data: PRequest,
        httpResponse: HttpResponse,
        ktorfit: Ktorfit
    ): Any

}