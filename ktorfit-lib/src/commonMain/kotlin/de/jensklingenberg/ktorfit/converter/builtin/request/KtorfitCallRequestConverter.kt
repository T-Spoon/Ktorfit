package de.jensklingenberg.ktorfit.converter.builtin.request

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Converter to enable the use of Call<> as return type
 * e.g. fun test(): Call<String>
 */
class KtorfitCallRequestConverter : RequestConverter {


    override fun supportedType(returnTypeName: TypeData): Boolean {
        return returnTypeName.qualifiedName == "de.jensklingenberg.ktorfit.Call"
    }

    override fun <RequestType> convertRequest(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<TypeInfo, HttpResponse?>,
        ktorfit: Ktorfit
    ): Any {
        return object : Call<RequestType> {
            override fun onExecute(callBack: Callback<RequestType>) {

                ktorfit.httpClient.launch {
                    val deferredResponse = async { requestFunction() }

                    try {
                        val (info, response) = deferredResponse.await()
                        val data = response!!.body(info) as RequestType
                        callBack.onResponse(data, response)
                    } catch (ex: Exception) {
                        callBack.onError(ex)
                    }

                }
            }

        }
    }

}

@Deprecated("Use KtorfitCallResponseConverter instead", ReplaceWith("KtorfitCallResponseConverter"))
typealias KtorfitSuspendCallResponseConverter = KtorfitCallRequestConverter