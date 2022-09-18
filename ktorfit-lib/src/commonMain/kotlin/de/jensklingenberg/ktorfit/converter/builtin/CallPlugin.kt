package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.converter.KtorfitPlugin
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.reflect.*

class CallPlugin() : KtorfitPlugin() {


    override fun getClass() = Call::class

    override fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Call<Any>> {
        val newInfo = TypeInfo(getClass(), info.reifiedType, info.kotlinType)
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

    override fun returnWhenException(exception: Exception) : Any?{
        val call = object : Call<Any> {
            override fun onExecute(callBack: Callback<Any>) {
                    callBack.onError(exception)
            }
        }
        return call
    }


}