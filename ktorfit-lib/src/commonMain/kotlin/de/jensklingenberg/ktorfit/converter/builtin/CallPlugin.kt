package de.jensklingenberg.ktorfit.converter.builtin

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.converter.MyMy
import de.jensklingenberg.ktorfit.internal.TypeData
import de.jensklingenberg.ktorfit.upperBoundType
import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.reflect.*

class CallPlugin() : MyMy() {
    override val key: AttributeKey<Test> = AttributeKey("MyPlugin3")
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

    override fun getClass() = Call::class


    override fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Call<Any>> {
        val newInfo = TypeInfo(getClass(), info.reifiedType, info.kotlinType)
        val call = object : Call<Any> {
            override fun onExecute(callBack: Callback<Any>) {
                if (exceptionFound == null) {
                    callBack.onResponse(body, response)
                } else {
                    callBack.onError(exceptionFound!!)
                }
            }

        }
        return newInfo to call
    }

    override fun install(plugin: Test, scope: HttpClient) {


        scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
            val result = if (info.type == getClass()) {
                foundCall = true
                HttpResponseContainer(info.upperBoundType()!!, body)
            } else {
                HttpResponseContainer(info, body)
            }


            proceedWith(result)
        }

        scope.responsePipeline.intercept(HttpResponsePipeline.Parse) { (info, body) ->
            if (foundCall) {

            }
            val tt = HttpResponseContainer(info, body)

            try {
                proceedWith(tt)
            } catch (ex: Exception) {
                exceptionFound = ex


                // proceedWith(tt)
                //ex
            }

            // this.context.response.status
        }

        scope.responsePipeline.intercept(HttpResponsePipeline.After) { (info, body) ->
            val response = this.context.response

            val result = if (foundCall) {
                val (newInfo, call) = convert(info, body, response)
                HttpResponseContainer(newInfo, call)
            } else {
                HttpResponseContainer(info, body)
            }

            this.context.response
            foundCall = false
            proceedWith(result)
        }

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