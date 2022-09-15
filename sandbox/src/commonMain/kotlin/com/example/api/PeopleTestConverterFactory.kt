package com.example.api

import com.example.model.Comment
import com.example.model.People
import com.example.model.Post
import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.internal.MyType
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class PeopleResponseConverter : ResponseConverter {
    override fun supportedType(returnTypeName: MyType): Boolean {
        return returnTypeName.packageName == "com.example.model.People"
    }


    override suspend fun convert(httpResponse: HttpResponse, data: Any?): Any {
        val str =  httpResponse.body<String>()
        var json = Json { ignoreUnknownKeys = true }
        val test =json.decodeFromString<People>(str)
        return test
    }

}

class CommentListResponseConverter : ResponseConverter {
    override fun supportedType(returnTypeName: MyType): Boolean {
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

class PostListResponseConverter : ResponseConverter {
    override fun supportedType(returnTypeName: MyType): Boolean {
        return (returnTypeName.packageName == "kotlin.collections.List") && (returnTypeName.typeArgs.firstOrNull()?.packageName?.contains(
            "Post"
        ) == true)
    }


    override suspend fun convert(httpResponse: HttpResponse, data: Any?): Any {
        val str =  httpResponse.body<String>()
        var json = Json { ignoreUnknownKeys = true }
        val test =json.decodeFromString<List<Post>>(str)
        return test
    }

}