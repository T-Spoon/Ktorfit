package de.jensklingenberg.ktorfit.demo

import de.jensklingenberg.ktorfit.converter.KtorfitPlugin
import de.jensklingenberg.ktorfit.converter.builtin.Response
import io.ktor.client.statement.*
import io.ktor.util.reflect.*

class ResponsePlugin() : KtorfitPlugin() {
    override fun getKey(): String {
        return "ResponsePlugin"
    }

    override fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Any> {
        val newInfo = TypeInfo(getClass(), info.reifiedType, info.kotlinType)
        val call = Response.success(body)
        return newInfo to call
    }

    override fun getClass() = Response::class

    override fun returnWhenException(exception: Exception) : Any{
        return Response.error(exception)
    }


}