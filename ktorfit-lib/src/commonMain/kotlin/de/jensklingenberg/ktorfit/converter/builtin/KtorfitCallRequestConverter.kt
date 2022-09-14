package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import de.jensklingenberg.ktorfit.converter.ResponseConverter
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Converter to enable the use of Call<> as return type
 * e.g. fun test(): Call<String>
 */
class KtorfitCallRequestConverter : RequestConverter{

    override fun supportedType(returnTypeName: String, isSuspend: Boolean): Boolean {
        return returnTypeName == "de.jensklingenberg.ktorfit.Call"
    }

    override fun <PRequest> convertResponse(
        returnTypeName: String,
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


@Deprecated("Use KtorfitCallResponseConverter instead", ReplaceWith("KtorfitCallResponseConverter"))
typealias KtorfitSuspendCallResponseConverter = KtorfitCallRequestConverter
