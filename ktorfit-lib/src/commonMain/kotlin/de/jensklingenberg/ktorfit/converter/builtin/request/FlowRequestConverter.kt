package de.jensklingenberg.ktorfit.converter.builtin.request

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.flow.flow

/**
 * Converter to enable the use of Flow<> as return type
 */
class FlowRequestConverter : RequestConverter {

    override fun supportedType(returnTypeName: TypeData): Boolean {
        return returnTypeName.qualifiedName == "kotlinx.coroutines.flow.Flow"
    }

    override fun <RequestType> convertRequest(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<TypeInfo, HttpResponse?>,
        ktorfit: Ktorfit
    ): Any {
        return flow {
            try {
                val (info, response) = requestFunction()
                emit(response!!.body(info))
            } catch (exception: Exception) {
                throw exception
            }
        }
    }
}
