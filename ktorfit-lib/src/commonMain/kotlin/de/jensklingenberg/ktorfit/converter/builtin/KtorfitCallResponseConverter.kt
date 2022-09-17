package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.*

/**
 * Converter to enable the use of Call<> as return type
 * e.g. fun test(): Call<String>
 */
class KtorfitCallResponseConverter : ResponseConverter {


    override fun supportedType(returnTypeName: TypeData): Boolean {
        return returnTypeName.packageName == "de.jensklingenberg.ktorfit.Call"
    }

    override suspend fun convert(httpResponse: HttpResponse, data: Any?): Call<Any> {
        return object : Call<Any> {
            override fun onExecute(callBack: Callback<Any>) {
                try {
                    callBack.onResponse(data!!, httpResponse)
                } catch (ex: Throwable) {
                    callBack.onError(ex)
                }
            }
        }
    }


}
