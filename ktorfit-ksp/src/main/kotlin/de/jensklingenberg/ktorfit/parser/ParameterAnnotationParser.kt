package de.jensklingenberg.ktorfit.parser

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSValueParameter
import de.jensklingenberg.ktorfit.model.KtorfitError
import de.jensklingenberg.ktorfit.model.annotations.ParameterAnnotation
import de.jensklingenberg.ktorfit.model.ktorfitError
import de.jensklingenberg.ktorfit.utils.*

/**
 *
 */
fun getParamAnnotationList(ksValueParameter: KSValueParameter, logger: KSPLogger): List<ParameterAnnotation> {
    val KEY_MAP = "Map"
    val KEY_STRING = "String"

    val paramAnnos = mutableListOf<ParameterAnnotation>()
    ksValueParameter.getBodyAnnotation()?.let {
        if (ksValueParameter.type.resolve().isMarkedNullable) {
            logger.ktorfitError(KtorfitError.BODY_PARAMETER_TYPE_MAY_NOT_BE_NULLABLE, ksValueParameter.type)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getPathAnnotation()?.let {
        if (ksValueParameter.type.resolve().isMarkedNullable) {
            logger.ktorfitError(KtorfitError.PATH_PARAMETER_TYPE_MAY_NOT_BE_NULLABLE, ksValueParameter.type)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getHeadersAnnotation()?.let {
        paramAnnos.add(it)
    }

    ksValueParameter.getHeaderMapAnnotation()?.let {
        //TODO: Find out how isAssignableFrom works
        if (!ksValueParameter.type.toString().endsWith(KEY_MAP)) {
            logger.ktorfitError(KtorfitError.HEADER_MAP_PARAMETER_TYPE_MUST_BE_MAP, ksValueParameter)
        }
        val mapKey = ksValueParameter.type.resolve().arguments.first()
        if (mapKey.type.toString() != KEY_STRING || mapKey.type?.resolve()?.isMarkedNullable == true) {
            logger.error(KtorfitError.HEADER_MAP_KEYS_MUST_BE_OF_TYPE_STRING, ksValueParameter)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getQueryAnnotation()?.let {
        paramAnnos.add(it)
    }

    ksValueParameter.getQueryNameAnnotation()?.let {
        paramAnnos.add(it)
    }

    ksValueParameter.getQueryMapAnnotation()?.let {
        if (!ksValueParameter.type.toString().endsWith(KEY_MAP)) {
            logger.error(KtorfitError.QUERY_MAP_PARAMETER_TYPE_MUST_BE_MAP, ksValueParameter)
        }

        val mapKey = ksValueParameter.type.resolve().arguments.first()
        if (mapKey.type.toString() != KEY_STRING || mapKey.type?.resolve()?.isMarkedNullable == true) {
            logger.error(KtorfitError.QUERY_MAP_KEYS_MUST_BE_OF_TYPE_STRING, ksValueParameter)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getFieldAnnotation()?.let {
        paramAnnos.add(it)
    }

    ksValueParameter.getFieldMapAnnotation()?.let {
        if (!ksValueParameter.type.toString().endsWith(KEY_MAP)) {
            logger.ktorfitError(KtorfitError.FIELD_MAP_PARAMETER_TYPE_MUST_BE_MAP, ksValueParameter)
        }

        val mapKey = ksValueParameter.type.resolve().arguments.first()
        if (mapKey.type.toString() != KEY_STRING || mapKey.type?.resolve()?.isMarkedNullable == true) {
            logger.error(KtorfitError.FIELD_MAP_KEYS_MUST_BE_OF_TYPE_STRING, ksValueParameter)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getPartAnnotation()?.let {
        if (ksValueParameter.type.resolve().isMarkedNullable) {
            logger.ktorfitError(KtorfitError.PART_PARAMETER_TYPE_MAY_NOT_BE_NULLABLE, ksValueParameter.type)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getPartMapAnnotation()?.let {
        if (!ksValueParameter.type.toString().endsWith(KEY_MAP)) {
            logger.ktorfitError(KtorfitError.PART_MAP_PARAMETER_TYPE_MUST_BE_MAP, ksValueParameter)
        }
        paramAnnos.add(it)
    }

    ksValueParameter.getUrlAnnotation()?.let {
        if (ksValueParameter.type.resolve().isMarkedNullable) {
            logger.ktorfitError(KtorfitError.URL_PARAMETER_TYPE_MAY_NOT_BE_NULLABLE, ksValueParameter)
        }
        paramAnnos.add(it)
    }
    return paramAnnos
}