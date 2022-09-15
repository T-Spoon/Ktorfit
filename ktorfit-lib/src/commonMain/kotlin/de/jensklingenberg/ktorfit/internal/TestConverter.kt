package de.jensklingenberg.ktorfit.internal

import de.jensklingenberg.ktorfit.converter.ResponseConverter
import io.ktor.client.call.*
import io.ktor.client.statement.*

class TestConverter : ResponseConverter {
    override fun supportedType(returnTypeName: MyType): Boolean {
        return returnTypeName.packageName == "kotlin.String"
    }


    override suspend fun convert(httpResponse: HttpResponse, data: Any?): String {
        return httpResponse.body<String>()
    }

}