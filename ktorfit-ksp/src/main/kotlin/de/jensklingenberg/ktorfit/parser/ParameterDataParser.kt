package de.jensklingenberg.ktorfit.parser

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSValueParameter
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.NO_KTORFIT_ANNOTATION_FOUND_AT
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.REQ_BUILDER_PARAMETER_TYPE_NEEDS_TO_BE_HTTP_REQUEST_BUILDER
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.VARARG_NOT_SUPPORTED_USE_LIST_OR_ARRAY
import de.jensklingenberg.ktorfit.model.ParameterData
import de.jensklingenberg.ktorfit.model.ReturnTypeData
import de.jensklingenberg.ktorfit.model.ktorfitError
import de.jensklingenberg.ktorfit.utils.*


fun getParameterData(ksValueParameter: KSValueParameter, logger: KSPLogger): ParameterData {
    if (ksValueParameter.isVararg) {
        logger.ktorfitError(VARARG_NOT_SUPPORTED_USE_LIST_OR_ARRAY, ksValueParameter)
    }

    val parameterAnnotations = getParamAnnotationList(ksValueParameter, logger)

    val reqBuilderAnno = ksValueParameter.getRequestBuilderAnnotation()
    val parameterName = ksValueParameter.name?.asString() ?: ""
    val parameterType = ksValueParameter.type.resolve()
    val hasRequestBuilderAnno = reqBuilderAnno != null

    if (parameterAnnotations.isEmpty() && !hasRequestBuilderAnno) {
        logger.ktorfitError(
            NO_KTORFIT_ANNOTATION_FOUND_AT(parameterName),
            ksValueParameter
        )
    }

    if (hasRequestBuilderAnno && parameterType.resolveTypeName() != "[@kotlin.ExtensionFunctionType] Function1<HttpRequestBuilder, Unit>") {
        logger.ktorfitError(
            REQ_BUILDER_PARAMETER_TYPE_NEEDS_TO_BE_HTTP_REQUEST_BUILDER,
            ksValueParameter
        )
    }

    val type = if (hasRequestBuilderAnno) {
        ReturnTypeData(
            "HttpRequestBuilder.()->Unit",
            "HttpRequestBuilder.()->Unit"
        )
    } else {
        ReturnTypeData(
            parameterType.resolveTypeName(),
            parameterType.declaration.qualifiedName?.asString() ?: ""
        )
    }

    return ParameterData(parameterName, type, parameterAnnotations, hasRequestBuilderAnno)

}




