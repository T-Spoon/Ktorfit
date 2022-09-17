package de.jensklingenberg.ktorfit.converter

import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.*
import io.ktor.client.statement.*

interface ResponseConverter {

    fun supportedType(returnTypeName: TypeData): Boolean

    suspend fun convert(httpResponse: HttpResponse, data: Any? = null): Any


}
