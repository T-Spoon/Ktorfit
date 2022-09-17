package de.jensklingenberg.ktorfit.demo

import de.jensklingenberg.ktorfit.converter.builtin.Response
import de.jensklingenberg.ktorfit.converter.builtin.Test
import de.jensklingenberg.ktorfit.upperBoundType
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.reflect.*

class MyPlugin(val prin: String) : HttpClientPlugin<Test.Config, Test> {
    override val key: AttributeKey<Test> = AttributeKey("MyPlugin")

    var foundCall = false

    fun getClass() = Response::class

    override fun install(plugin: Test, scope: HttpClient) {
        println("INSTALLED" + prin)


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
           if(foundCall){

           }
            this.context.response.status
        }

        scope.responsePipeline.intercept(HttpResponsePipeline.After) { (info, body) ->
            val result = if (foundCall) {
                val newInfo = TypeInfo(getClass(), info.reifiedType, info.kotlinType)
                val call = Response.success(body)
                HttpResponseContainer(newInfo, call)
            } else {
                HttpResponseContainer(info, body)
            }

            this.context.response
            foundCall = false
            proceedWith(result)
        }

    }

    override fun prepare(block: Test.Config.() -> Unit): Test {
        return Test()
    }
}