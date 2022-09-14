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
class KtorfitCallResponseConverter :  ResponseConverter {

    override fun <PRequest> convertResponse(
        returnTypeName: String,
        data: PRequest,
        httpResponse: HttpResponse,
        ktorfit: Ktorfit
    ): Any {

        return object : Call<PRequest> {
            override fun onExecute(callBack: Callback<PRequest>) {
                try {
                    callBack.onResponse(data, httpResponse)
                } catch (ex: Throwable) {
                    callBack.onError(ex)
                }
            }
        }
    }

    override fun supportedType(returnTypeName: String, isSuspend: Boolean): Boolean {
        return returnTypeName == "de.jensklingenberg.ktorfit.Call"
    }



}