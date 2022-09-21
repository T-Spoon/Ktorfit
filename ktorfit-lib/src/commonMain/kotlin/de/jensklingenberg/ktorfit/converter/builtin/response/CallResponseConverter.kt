package de.jensklingenberg.ktorfit.converter.builtin.response

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import io.ktor.client.statement.*
import io.ktor.util.reflect.*

class CallResponseConverter : ResponseConverter() {

    override fun pluginForClass() = Call::class

    override fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Call<Any>> {
        val newInfo = TypeInfo(pluginForClass(), info.reifiedType, info.kotlinType)
        val call = object : Call<Any> {
            override fun onExecute(callBack: Callback<Any>) {
                    callBack.onResponse(body, response)
            }
        }
        return newInfo to call
    }

    override fun getKey(): String {
        return "CallPlugin"
    }

    override fun onErrorReturn(exception: Exception) : Any {
        val call = object : Call<Any> {
            override fun onExecute(callBack: Callback<Any>) {
                    callBack.onError(exception)
            }
        }
        return call
    }


}