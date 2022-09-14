package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.KConverter
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import io.ktor.client.statement.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.flow.flow

/**
 * Converter to enable the use of Flow<> as return type
 */
class FlowRequestConverter : RequestConverter {

    override fun supportedType(returnTypeName: String, isSuspend: Boolean): Boolean {
        return returnTypeName == "kotlinx.coroutines.flow.Flow"
    }

    override fun <PRequest> convertResponse(
        returnTypeName: String,
        requestFunction: suspend () -> Pair<PRequest, HttpResponse>,
        ktorfit: Ktorfit
    ): Any {
        return flow {
            try {
                val (info, response) = requestFunction()
                emit(info)
            } catch (exception: Exception) {
                throw exception
            }
        }
    }
}

interface RequestConverter{
    fun requestBodyConverter(type: String, ktorfit: Ktorfit): KConverter<HttpResponse, *>? = null

}

