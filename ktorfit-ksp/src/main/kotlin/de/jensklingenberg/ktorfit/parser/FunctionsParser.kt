package de.jensklingenberg.ktorfit.parser

import KtorfitProcessor
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import de.jensklingenberg.ktorfit.model.*
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.BODY_PARAMETERS_CANNOT_BE_USED_WITH_FORM_OR_MULTI_PART_ENCODING
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.FIELD_MAP_PARAMETERS_CAN_ONLY_BE_USED_WITH_FORM_ENCODING
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.FIELD_PARAMETERS_CAN_ONLY_BE_USED_WITH_FORM_ENCODING
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.FORM_URL_ENCODED_CAN_ONLY_BE_SPECIFIED_ON_HTTP_METHODS_WITH_REQUEST_BODY
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.FOR_STREAMING_THE_RETURN_TYPE_MUST_BE_HTTP_STATEMENT
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.HEADERS_VALUE_MUST_BE_IN_FORM
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.MISSING_EITHER_KEYWORD_URL_OrURL_PARAMETER
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.MISSING_X_IN_RELATIVE_URL_PATH
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.MULTIPART_CAN_ONLY_BE_SPECIFIED_ON_HTTPMETHODS
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.MULTIPLE_URL_METHOD_ANNOTATIONS_FOUND
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.NON_BODY_HTTP_METHOD_CANNOT_CONTAIN_BODY
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.NO_HTTP_ANNOTATION_AT
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.ONLY_ONE_ENCODING_ANNOTATION_IS_ALLOWED
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.ONLY_ONE_HTTP_METHOD_IS_ALLOWED
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.ONLY_ONE_REQUEST_BUILDER_IS_ALLOWED
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.PATH_CAN_ONLY_BE_USED_WITH_RELATIVE_URL_ON
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.URL_CAN_ONLY_BE_USED_WITH_EMPY
import de.jensklingenberg.ktorfit.model.annotations.*
import de.jensklingenberg.ktorfit.utils.*

/**
 * Collect all [HttpMethodAnnotation] from a [KSFunctionDeclaration]
 * @return list of [HttpMethodAnnotation]
 */
fun getHttpMethodAnnotations(ksFunctionDeclaration: KSFunctionDeclaration): List<HttpMethodAnnotation> {
    val getAnno = ksFunctionDeclaration.parseHTTPMethodAnno("GET")
    val putAnno = ksFunctionDeclaration.parseHTTPMethodAnno("PUT")
    val postAnno = ksFunctionDeclaration.parseHTTPMethodAnno("POST")
    val deleteAnno = ksFunctionDeclaration.parseHTTPMethodAnno("DELETE")
    val headAnno = ksFunctionDeclaration.parseHTTPMethodAnno("HEAD")
    val optionsAnno = ksFunctionDeclaration.parseHTTPMethodAnno("OPTIONS")
    val patchAnno = ksFunctionDeclaration.parseHTTPMethodAnno("PATCH")
    val httpAnno = ksFunctionDeclaration.parseHTTPMethodAnno("HTTP")

    return listOfNotNull(getAnno, postAnno, putAnno, deleteAnno, headAnno, optionsAnno, patchAnno, httpAnno)
}

fun getFunctionDataList(
    ksFunctionDeclarationList: List<KSFunctionDeclaration>,
    logger: KSPLogger,
    imports: List<String>,
    packageName: String
): List<FunctionData> {

    return ksFunctionDeclarationList.map { funcDeclaration ->

        val functionName = funcDeclaration.simpleName.asString()
        val functionParameters = funcDeclaration.parameters.map { getParameterData(it, logger) }

        val typeData = getMyType(
            funcDeclaration.returnType?.resolve().resolveTypeName().removeWhiteSpaces(),
            imports,
            packageName,
            KtorfitProcessor.rresolver
        )

        val returnType = ReturnTypeData(
            funcDeclaration.returnType?.resolve().resolveTypeName(),
            typeData.toString()
        )

        val functionAnnotationList = mutableListOf<FunctionAnnotation>()

        funcDeclaration.getMultipartAnnotation()?.let {
            functionAnnotationList.add(it)
        }

        if (funcDeclaration.typeParameters.isNotEmpty()) {
            logger.ktorfitError(
                KtorfitError.FUNCTION_OR_PARAMETERS_TYPES_MUST_NOT_INCLUDE_ATYPE_VARIABLE_OR_WILDCARD,
                funcDeclaration
            )
        }

        funcDeclaration.getHeadersAnnotation()?.let { headers ->
            headers.path.forEach {
                //Check if headers are in valid format
                try {
                    val (key, value) = it.split(":")
                } catch (exception: Exception) {
                    logger.ktorfitError(HEADERS_VALUE_MUST_BE_IN_FORM + it, funcDeclaration)
                }
            }
            functionAnnotationList.add(headers)
        }

        funcDeclaration.getFormUrlEncodedAnnotation()?.let { formUrlEncoded ->
            val isWithoutFieldOrFieldMap =
                functionParameters.none { it.hasAnnotation<Field>() || it.hasAnnotation<FieldMap>() }
            if (isWithoutFieldOrFieldMap) {
                logger.ktorfitError(
                    KtorfitError.FORM_ENCODED_METHOD_MUST_CONTAIN_AT_LEAST_ONE_FIELD_OR_FIELD_MAP,
                    funcDeclaration
                )
            }

            if (funcDeclaration.getMultipartAnnotation() != null) {
                logger.ktorfitError(ONLY_ONE_ENCODING_ANNOTATION_IS_ALLOWED, funcDeclaration)
            }

            functionAnnotationList.add(formUrlEncoded)
        }

        funcDeclaration.getStreamingAnnotation()?.let { streaming ->
            val returnsHttpStatement = returnType.name == "HttpStatement"
            if (!returnsHttpStatement) {
                logger.ktorfitError(
                    FOR_STREAMING_THE_RETURN_TYPE_MUST_BE_HTTP_STATEMENT,
                    funcDeclaration
                )
            }
            functionAnnotationList.add(streaming)
        }

        val httpMethodAnnoList = getHttpMethodAnnotations(funcDeclaration)

        if (httpMethodAnnoList.isEmpty()) {
            logger.ktorfitError(NO_HTTP_ANNOTATION_AT(functionName), funcDeclaration)
        }

        if (httpMethodAnnoList.size > 1) {
            logger.ktorfitError(ONLY_ONE_HTTP_METHOD_IS_ALLOWED + "Found: " + httpMethodAnnoList.joinToString { it.httpMethod.keyword } + " at " + functionName,
                funcDeclaration)
        }

        val firstHttpMethodAnnotation = httpMethodAnnoList.first()

        val isEmptyHttpPathWithoutUrlAnnotation =
            firstHttpMethodAnnotation.path.isEmpty() && functionParameters.none { it.hasAnnotation<Url>() }
        if (isEmptyHttpPathWithoutUrlAnnotation) {
            logger.ktorfitError(
                MISSING_EITHER_KEYWORD_URL_OrURL_PARAMETER(firstHttpMethodAnnotation.httpMethod.keyword),
                funcDeclaration
            )
        }

        if (functionParameters.filter { it.hasRequestBuilderAnno }.size > 1) {
            logger.ktorfitError(ONLY_ONE_REQUEST_BUILDER_IS_ALLOWED + " Found: " + httpMethodAnnoList.joinToString { it.toString() } + " at " + functionName,
                funcDeclaration)
        }

        when (firstHttpMethodAnnotation.httpMethod) {
            HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH -> {}
            else -> {
                if (firstHttpMethodAnnotation is CustomHttp && firstHttpMethodAnnotation.hasBody) {
                    //Do nothing
                } else if (functionParameters.any { it.hasAnnotation<Body>() }) {
                    logger.ktorfitError(NON_BODY_HTTP_METHOD_CANNOT_CONTAIN_BODY, funcDeclaration)
                }

                if (functionAnnotationList.any { it is Multipart }) {
                    logger.ktorfitError(
                        MULTIPART_CAN_ONLY_BE_SPECIFIED_ON_HTTPMETHODS,
                        funcDeclaration
                    )
                }

                if (funcDeclaration.getFormUrlEncodedAnnotation() != null) {
                    logger.ktorfitError(
                        FORM_URL_ENCODED_CAN_ONLY_BE_SPECIFIED_ON_HTTP_METHODS_WITH_REQUEST_BODY,
                        funcDeclaration
                    )
                }
            }
        }

        if (functionParameters.any { it.hasAnnotation<Path>() } && firstHttpMethodAnnotation.path.isEmpty()) {
            logger.ktorfitError(
                PATH_CAN_ONLY_BE_USED_WITH_RELATIVE_URL_ON + "@${firstHttpMethodAnnotation.httpMethod.keyword}",
                funcDeclaration
            )
        }

        functionParameters.filter { it.hasAnnotation<Path>() }.forEach {
            val pathAnnotation = it.findAnnotationOrNull<Path>()
            if (!firstHttpMethodAnnotation.path.contains("{${pathAnnotation?.value ?: ""}}")) {
                logger.ktorfitError(
                    MISSING_X_IN_RELATIVE_URL_PATH(pathAnnotation?.value ?: ""),
                    funcDeclaration
                )
            }
        }

        if (functionParameters.any { it.hasAnnotation<Url>() }) {
            if (functionParameters.filter { it.hasAnnotation<Url>() }.size > 1) {
                logger.ktorfitError(MULTIPLE_URL_METHOD_ANNOTATIONS_FOUND, funcDeclaration)
            }
            if (firstHttpMethodAnnotation.path.isNotEmpty()) {
                logger.ktorfitError(
                    URL_CAN_ONLY_BE_USED_WITH_EMPY(firstHttpMethodAnnotation.httpMethod.keyword),
                    funcDeclaration
                )
            }
        }

        if (functionParameters.any { it.hasAnnotation<Field>() } && funcDeclaration.getFormUrlEncodedAnnotation() == null) {
            logger.ktorfitError(FIELD_PARAMETERS_CAN_ONLY_BE_USED_WITH_FORM_ENCODING, funcDeclaration)
        }

        if (functionParameters.any { it.hasAnnotation<FieldMap>() } && funcDeclaration.getFormUrlEncodedAnnotation() == null) {
            logger.ktorfitError(FIELD_MAP_PARAMETERS_CAN_ONLY_BE_USED_WITH_FORM_ENCODING, funcDeclaration)
        }

        if (functionParameters.any { it.hasAnnotation<Body>() } && funcDeclaration.getFormUrlEncodedAnnotation() != null) {
            logger.ktorfitError(BODY_PARAMETERS_CANNOT_BE_USED_WITH_FORM_OR_MULTI_PART_ENCODING, funcDeclaration)
        }

        return@map FunctionData(
            functionName,
            returnType,
            funcDeclaration.isSuspend,
            functionParameters,
            functionAnnotationList,
            firstHttpMethodAnnotation
        )

    }
}

private fun String.removeWhiteSpaces(): String {
    return this.replace("\\s".toRegex(), "")
}
