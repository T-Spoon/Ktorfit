package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Converter to enable the use of Call<> as return type
 * e.g. fun test(): Call<String>
 */
class DefferdRequestConverter : RequestConverter {


    override fun supportedType(returnTypeName: TypeData): Boolean {
        return returnTypeName.packageName == "de.jensklingenberg.ktorfit.Call"
    }

    override fun <PRequest> convertRequest(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<PRequest, HttpResponse>,
        ktorfit: Ktorfit
    ): Any {



        return object : Call<PRequest> {
            override fun onExecute(callBack: Callback<PRequest>) {

                GlobalScope.launch {
                    val deferredResponse = async { requestFunction() }

                    try {
                        val (data, response) = deferredResponse.await()
                        callBack.onResponse(data, response)
                    } catch (ex: Exception) {
                        callBack.onError(ex)
                    }

                }
            }

        }
    }

}