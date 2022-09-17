package com.example.api

import com.example.model.Comment
import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CommentListResponseConverter : ResponseConverter {
    override fun supportedType(returnTypeName: TypeData): Boolean {
        return (returnTypeName.packageName == "kotlin.collections.List") && (returnTypeName.typeArgs.firstOrNull()?.packageName?.contains(
            "Comment"
        ) == true)
    }


    override suspend fun convert(httpResponse: HttpResponse, data: Any?): Any {
        val str =  httpResponse.body<String>()
        var json = Json { ignoreUnknownKeys = true }
        val test =json.decodeFromString<List<Comment>>(str)
        return test
    }



}