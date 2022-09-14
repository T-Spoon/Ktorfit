package de.jensklingenberg.ktorfit.demo

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.ResponseConverter
import io.ktor.client.statement.*

class Converter : ResponseConverter {
    override fun <PRequest> convertResponse(
        returnTypeName: String,
        data: PRequest,
        httpResponse: HttpResponse,
        ktorfit: Ktorfit
    ): Any {
        return try {
            Response.success<Any>(data as Any)
        } catch (ex: Throwable) {
            Response.error(ex)
        }
    }

    override fun supportedType(returnTypeName: String, isSuspend: Boolean): Boolean {
        return returnTypeName.equals("de.jensklingenberg.ktorfit.demo.Response", true)
    }

}