package de.jensklingenberg.ktorfit.demo

import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.converter.builtin.Response
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.*

class MyResponseConverter : ResponseConverter {
    override fun supportedType(returnTypeName: TypeData): Boolean {
        return returnTypeName.packageName == "de.jensklingenberg.ktorfit.converter.builtin.Response"
    }


    override suspend fun convert(httpResponse: HttpResponse, data: Any?): Response<*> {
        return try {
            Response.error(NullPointerException())
        } catch (ex: Throwable) {
            Response.error(ex)
        }
    }

}