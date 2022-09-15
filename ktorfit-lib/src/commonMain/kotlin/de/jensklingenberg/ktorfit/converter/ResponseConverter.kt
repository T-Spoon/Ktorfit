package de.jensklingenberg.ktorfit.converter

import de.jensklingenberg.ktorfit.internal.MyType
import io.ktor.client.statement.*

interface ResponseConverter {

    fun supportedType(returnTypeName: MyType): Boolean

    suspend fun convert(httpResponse: HttpResponse, data: Any? = null): Any
}