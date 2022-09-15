package de.jensklingenberg.ktorfit.internal

import de.jensklingenberg.ktorfit.converter.ResponseConverter
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.reflect.*
import kotlin.reflect.KClass

class MyType(val packageName: String, val typeArgs: List<MyType> = emptyList())

fun getti(text: String): MyType {
    var className = text.substringBefore("<", "")
    if (className.isEmpty()) {
        className = text.substringBefore(",", "")
    }
    if (className.isEmpty()) {
        className = text
    }
    val type = (text.removePrefix(className)).substringAfter("<").substringBeforeLast(">")
    var typo = mutableListOf<MyType>()
    if (type.contains("<")) {
        typo.add(getti(type))
    } else if (type.contains(",")) {
        type.split(",").forEach {
            typo.add(getti(it))
        }
    } else if (type.isNotEmpty()) {
        typo.add(getti(type))
    }

    return MyType(className, typo)
}


class Person<T : Any>(val clazz: KClass<T>)

inline fun <reified T : Any> Person(): Person<T> {
    return Person(T::class)
}

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

    fun findConverter(
        kResponseConverter: Set<ResponseConverter>,
        qualifiedRawTypeName: MyType,
        ktorfit: Ktorfit
    ): ResponseConverter? {
        return kResponseConverter.firstOrNull() { it.supportedType(qualifiedRawTypeName) }

    }


    /**
     * This will handle all requests for functions with suspend modifier
     * Used by generated Code
     */
    suspend inline fun <reified TReturn, reified PRequest : Any?> suspendRequest(
        requestData: RequestData
    ): TReturn? {
        //val ty = getti(requestData.qualifiedRawTypeName)
        val tt = typeInfo<TReturn>().type!!
        // println("HEY"+ty)


        if (TReturn::class == HttpStatement::class) {
            return httpClient.prepareRequest {
                requestBuilder(requestData)
            } as TReturn
        }


        val response = httpClient.request {
            requestBuilder(requestData)
        }



        val outerConverter = findConverter(ktorfit.kResponseConverter, requestData.qualifiedRawTypeName, ktorfit)

        val next = requestData.qualifiedRawTypeName.typeArgs.first()

        val innerConverter = findConverter(ktorfit.kResponseConverter, next, ktorfit)


        val a1 = innerConverter?.convert(response) ?: response.body<PRequest>()

        return outerConverter?.convert(response, a1) as TReturn
        if (innerConverter == null) {
            ktorfit.kResponseConverter.firstOrNull { it.supportedType(next) }
        }

        ktorfit.kResponseConverter.firstOrNull { converter ->
            converter.supportedType(
                requestData.qualifiedRawTypeName
            )
        }?.let {
            return it.convert(
                data = response.body<PRequest>(),
                httpResponse = response,
            ) as TReturn
        }

        return try {
            ktorfit.kResponseConverter.firstOrNull {
                it.supportedType(requestData.qualifiedRawTypeName) != null
            }?.let {
                return it.convert(response, ktorfit)!!
                        as TReturn
            } ?: response.body<TReturn>()
        } catch (exception: Exception) {
            val typeIsNullable = requestData.qualifiedRawTypeName.packageName.endsWith("?")
            return if (typeIsNullable) {
                null
            } else {
                throw exception
            }

        }

    }


    //T is return type
    //P is requested type
    /**
     * This will handle all requests for functions without suspend modifier
     */
    inline fun <reified TReturn, reified PRequest : Any?> request(
        requestData: RequestData
    ): TReturn? {

        val reqConv = ktorfit.requestConverters.firstOrNull { converter ->
            converter.supportedType(
                requestData.qualifiedRawTypeName
            )
        }

        reqConv?.let { requetConv ->
            return requetConv.convertRequest<PRequest?>(
                returnType = requestData.qualifiedRawTypeName,
                requestFunction = {

                    val response = httpClient.request {
                        requestBuilder(requestData)
                    }

                    val kConv = ktorfit.kResponseConverter.firstOrNull {
                        it.supportedType(requestData.qualifiedRawTypeName.typeArgs.first())
                    }

                    kConv?.let {
                        println("===============   " + it)
                        val gg = it.convert(
                            response,
                            ktorfit
                        ) as PRequest
                        Pair(gg, response)
                    } ?: Pair(
                        response.body(),
                        response
                    )

                },
                ktorfit = ktorfit
            ) as TReturn
        }


        val typeIsNullable = requestData.qualifiedRawTypeName.packageName.endsWith("?")
        return if (typeIsNullable) {
            null
        } else {
            throw IllegalArgumentException("Add a ResponseConverter for " + requestData.qualifiedRawTypeName + " or make function suspend")

        }

    }


    fun HttpRequestBuilder.requestBuilder(
        requestData: RequestData
    ) {

        handleHeaders(requestData.headers)
        handleFields(requestData.fields)
        handleParts(requestData.parts)
        this.method = HttpMethod.parse(requestData.method)

        handleBody(requestData.bodyData)
        handleQueries(requestData)
        val queryNameUrl = handleQueryNames(requestData)

        val relativeUrl = getRelativeUrl(requestData.paths, requestData.relativeUrl).removePrefix(ktorfit.baseUrl)

        url(ktorfit.baseUrl + relativeUrl + queryNameUrl)

        requestData.requestBuilder(this)
    }

    private fun HttpRequestBuilder.handleBody(body: Any?) {
        body?.let {
            setBody(it)
        }
    }

    /**
     * This method replaces all parts of the [relativeUrl] which have curly braces
     * with their corresponding value
     * @return the relative URL with replaced values
     */
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
                        for ((key, value) in data.entries) {
                            append(key.toString(), value.toString())
                        }
                    }

                    null -> {
                        //Ignore this header
                    }

                    else -> {
                        append(it.key, it.value.toString())
                    }
                }
            }
        }
    }

    private fun HttpRequestBuilder.handleQueries(requestData: RequestData) {
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

                null -> {
                    //Ignore this query
                }

                else -> {
                    setParameter(entry.encoded, entry.key, entry.data.toString())
                }
            }
        }

        requestData.queries.filter { it.type == QueryType.QUERYMAP }.forEach { entry ->
            for ((key, value) in entry.data as Map<*, *>) {
                value?.let {
                    setParameter(entry.encoded, key.toString(), value.toString())
                }

            }
        }

    }

    /**
     * Returns a String for the query names that will be appended to the url
     * QueryNames will be handled special because otherwise Ktor always adds a "=" behind every
     * query e.g. QueryName("Hello") will be sent by Ktor like "?Hello="
     */
    private fun HttpRequestBuilder.handleQueryNames(requestData: RequestData): String {
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

                null -> {
                    //Ignore this query
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


    private fun HttpRequestBuilder.encodedParameter(key: String, value: Any): Unit =
        value.let { url.encodedParameters.append(key, it.toString()) } ?: Unit

}
