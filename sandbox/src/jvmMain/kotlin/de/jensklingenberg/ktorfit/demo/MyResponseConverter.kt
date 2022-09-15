package de.jensklingenberg.ktorfit.demo

import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.internal.MyType
import io.ktor.client.statement.*

class MyResponseConverter : ResponseConverter {
    override fun supportedType(returnTypeName: MyType): Boolean {
        return returnTypeName.packageName == "de.jensklingenberg.ktorfit.demo.Response"
    }


    override suspend fun convert(httpResponse: HttpResponse, data: Any?): Response<*> {
        return try {
            Response.success(data)
        } catch (ex: Throwable) {
            Response.error(ex)
        }
    }

}