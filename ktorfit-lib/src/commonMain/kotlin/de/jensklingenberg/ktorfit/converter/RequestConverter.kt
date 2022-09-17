package de.jensklingenberg.ktorfit.converter

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.*

/**
 * Implement this to support wrapping for custom types
 *  e.g. fun test() : MyCustomType<String>
 */
interface RequestConverter : CoreResponseConverter {

    /**
     * @param typeData is the qualified name of the outer type of
     * @param requestFunction a suspend function that will return a typeInfo of Ktor's requested type and the [HttpResponse]
     * the return type. e.g. for Flow<String> it will be kotlinx.coroutines.flow.Flow
     * @return the wrapped response
     */
    fun <PRequest : Any?> convertRequest(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<PRequest, HttpResponse>,
        ktorfit: Ktorfit
    ): Any

}
