package de.jensklingenberg.ktorfit.demo

import com.example.api.Response
import de.jensklingenberg.ktorfit.converter.response.ResponseConverterPlugin
import io.ktor.client.statement.*
import io.ktor.util.reflect.*

class ResponseResponseConverterPlugin() : ResponseConverterPlugin() {
    override fun getKey(): String {
        return "ResponsePlugin"
    }

    override fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Any> {
        val newInfo = TypeInfo(pluginForClass(), info.reifiedType, info.kotlinType)
        val call = Response.success(body)
        return newInfo to call
    }

    override fun pluginForClass() = Response::class

    override fun onErrorReturn(exception: Exception): Any? {
        return Response.error(exception)
    }

}