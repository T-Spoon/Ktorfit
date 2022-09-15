package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import de.jensklingenberg.ktorfit.internal.MyType
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.flow

/**
 * Converter to enable the use of Flow<> as return type
 */
class FlowRequestConverter : RequestConverter {

    override fun supportedType(returnTypeName: MyType): Boolean {
        return returnTypeName.packageName == "kotlinx.coroutines.flow.Flow"
    }

    override fun <PRequest> convertRequest(
        returnType: MyType,
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
