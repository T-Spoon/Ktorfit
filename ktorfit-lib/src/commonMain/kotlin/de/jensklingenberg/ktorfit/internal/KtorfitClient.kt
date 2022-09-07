package de.jensklingenberg.ktorfit.internal

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.reflect.*


/**
 * This class will be used by the generated Code
 * Please don't use the class directly
 */
@InternalKtorfitApi
class KtorfitClient(val ktorfit: Ktorfit) {

   val httpClient = ktorfit.httpClient

    /**
     * Converts [value] to an URL encoded value
     */
    private fun encode(value: Any): String {
        return value.toString().encodeURLParameter()
    }

    /**
     * This will handle all requests for functions with suspend modifier
     * Used by generated Code
     */
    suspend inline fun <reified TReturn, reified PRequest : Any> suspendRequest(
        requestData: RequestData
    ): TReturn {

        if (TReturn::class == HttpStatement::class) {
            return httpClient.prepareRequest {
                requestBuilder(requestData)
            } as TReturn
        }

        val request = httpClient.request {
            requestBuilder(requestData)
        }

        ktorfit.suspendResponseConverters.firstOrNull { converter ->
            converter.supportedType(
                requestData.qualifiedRawTypeName,
                true
            )
        }?.let {
            return it.wrapSuspendResponse<PRequest>(
                returnTypeName = requestData.qualifiedRawTypeName,
                requestFunction = {
                    Pair(typeInfo<PRequest>(), request)
                }) as TReturn
        }

        return request.body()

    }


    //T is return type
    //P is requested type
    /**
     * This will handle all requests for functions without suspend modifier
     */
    inline fun <reified TReturn, reified PRequest : Any> request(
        requestData: RequestData
    ): TReturn {

        ktorfit.responseConverters.firstOrNull { converter ->
            converter.supportedType(
                requestData.qualifiedRawTypeName,
                false
            )
        }?.let {
            return it.wrapResponse<PRequest>(
                returnTypeName = requestData.qualifiedRawTypeName,
                requestFunction = {
                val response = httpClient.request {
                    requestBuilder(requestData)
                }
                Pair(typeInfo<PRequest>(), response)
            }) as TReturn
        }

        throw IllegalArgumentException("Add a ResponseConverter for " + requestData.qualifiedRawTypeName + " or make function suspend")
    }


    fun HttpRequestBuilder.requestBuilder(
        requestData: RequestData
    ) {

        handleHeaders(requestData.headers)
        handleFields(requestData.fields)
        handleParts(requestData.parts)
        this.method = HttpMethod.parse(requestData.method)

        requestData.bodyData?.let {
            setBody(it)
        }

        val queryNameUrl = handleQueries(requestData)

        val newURL = getRelativeUrl(requestData.paths, requestData.relativeUrl)

        url(ktorfit.baseUrl + newURL + queryNameUrl)

        requestData.requestBuilder(this)
    }

    private fun getRelativeUrl(paths: List<PathData>, relativeUrl: String): String {
        var newUrl = relativeUrl
        paths.forEach {

            val newPathValue = if (it.encoded) {
                it.value
            } else {
                encode(it.value)
            }

            newUrl = newUrl.replace("{${it.key}}", newPathValue)
        }

        return newUrl
    }

    private fun HttpRequestBuilder.handleHeaders(headers: List<HeaderData>) {
        headers {
            headers.forEach {
                when (val data = it.value) {
                    is List<*> -> {
                        data.filterNotNull().forEach { dataEntry ->
                            append(it.key, dataEntry.toString())
                        }
                    }

                    is Array<*> -> {
                        data.filterNotNull().forEach { dataEntry ->
                            append(it.key, dataEntry.toString())
                        }
                    }

                    is Map<*, *> -> {
                        data.entries.forEach { entry ->
                            append(entry.key.toString(), entry.value.toString())
                        }
                    }

                    else -> {
                        append(it.key, it.value.toString())
                    }
                }
            }
        }
    }

    private fun HttpRequestBuilder.handleQueries(requestData: RequestData): String {
        val queryNames = mutableListOf<String>()
        requestData.queries.filter { it.type == QueryType.QUERYNAME }.forEach { entry ->
            when (val data = entry.data) {
                is List<*> -> {
                    data.filterNotNull().forEach { dataEntry ->
                        if (entry.encoded) {
                            queryNames.add(dataEntry.toString())
                        } else {
                            queryNames.add(encode(dataEntry.toString()))
                        }
                    }
                }

                is Array<*> -> {
                    data.filterNotNull().forEach { dataEntry ->
                        if (entry.encoded) {
                            queryNames.add(dataEntry.toString())
                        } else {
                            queryNames.add(encode(dataEntry.toString()))
                        }
                    }
                }

                else -> {
                    if (entry.encoded) {
                        queryNames.add(entry.data.toString())
                    } else {
                        queryNames.add(encode(entry.data.toString()))
                    }
                }
            }
        }
        var queryNameUrl = queryNames.joinToString("&") { it }

        queryNameUrl = ("?$queryNameUrl").takeIf { queryNameUrl.isNotEmpty() } ?: ""

        requestData.queries.filter { it.type == QueryType.QUERY }.forEach { entry ->

            when (val data = entry.data) {
                is List<*> -> {
                    data.filterNotNull().forEach {
                        setParameter(entry.encoded, entry.key, it.toString())
                    }
                }

                is Array<*> -> {
                    data.filterNotNull().forEach {
                        setParameter(entry.encoded, entry.key, it.toString())
                    }
                }

                else -> {
                    setParameter(entry.encoded, entry.key, entry.data.toString())
                }
            }
        }

        requestData.queries.filter { it.type == QueryType.QUERYMAP }.forEach { entry ->
            (entry.data as Map<*, *>).forEach {
                setParameter(entry.encoded, it.key.toString(), it.value.toString())
            }
        }
        return queryNameUrl
    }

    private fun HttpRequestBuilder.handleFields(fields: List<FieldData>) {
        if (fields.isNotEmpty()) {
            val formParameters = Parameters.build {

                fun append(encoded: Boolean, key: String, value: String) {
                    if (encoded) {
                        append(key, value)
                    } else {
                        append(encode(key), encode(value))
                    }
                }

                fields.filter { it.type == FieldType.FIELD }.forEach { entry ->

                    when (val data = entry.data) {
                        is List<*> -> {
                            data.filterNotNull().forEach {
                                append(entry.encoded, entry.key, it as String)
                            }
                        }

                        else -> {
                            append(entry.encoded, entry.key, entry.data.toString())
                        }
                    }
                }

                fields.filter { it.type == FieldType.FIELDMAP }.forEach { entry ->
                    (entry.data as Map<*, *>).forEach {
                        append(entry.encoded, it.key.toString(), it.value.toString())
                    }
                }
            }
            setBody(FormDataContent(formParameters))

        }
    }

    @OptIn(InternalAPI::class)
    private fun HttpRequestBuilder.handleParts(parts: Map<String, Any>) {
        if (parts.isNotEmpty()) {
            val partDatas = mutableListOf<PartData>()

            parts.forEach {
                if (it.value as? List<PartData> != null) {
                    partDatas.addAll(it.value as List<PartData>)
                }
            }

            val formData = formData {
                parts.filter { it.value is String }.forEach {
                    this@formData.append(it.key, it.value)
                }
            }
            val partDataList = formData + partDatas
            setBody(MultiPartFormDataContent(partDataList))
        }
    }

    private fun HttpRequestBuilder.setParameter(
        encoded: Boolean,
        key: String,
        value: String
    ) {
        if (encoded) {
            encodedParameter(key, value)
        } else {
            parameter(key, (value))
        }
    }


    private fun HttpRequestBuilder.encodedParameter(key: String, value: Any?): Unit =
        value?.let { url.encodedParameters.append(key, it.toString()) } ?: Unit

}
