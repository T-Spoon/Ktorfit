package de.jensklingenberg.ktorfit.generator

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import de.jensklingenberg.ktorfit.model.ClassData
import de.jensklingenberg.ktorfit.model.clientClass
import de.jensklingenberg.ktorfit.model.requestDataClass
import de.jensklingenberg.ktorfit.requestData.getRequestDataArgumentText

fun getFunSpecs(classData: ClassData): List<FunSpec> = classData.functions.map { functionData ->

    val returnTypeName = functionData.returnType.name
    val typeWithoutOuterType = returnTypeName.substringAfter("<").substringBeforeLast(">")
    val nullableText = if (functionData.returnType.name.endsWith("?")) {
        ""
    } else {
        "!!"
    }
    FunSpec.builder(functionData.name)
        .addModifiers(mutableListOf(KModifier.OVERRIDE).also {
            if (functionData.isSuspend) {
                it.add(KModifier.SUSPEND)
            }
        })
        .returns(TypeVariableName(functionData.returnType.name))
        .addParameters(functionData.parameterDataList.map {
            ParameterSpec(it.name, TypeVariableName(it.type.name))
        })
        .addStatement(
            getRequestDataArgumentText(
                functionData,
            )
        )
        .addStatement(
            if (functionData.isSuspend) {
                "return ${clientClass.objectName}.suspendRequest<${returnTypeName}>(${requestDataClass.objectName})" + nullableText
            } else {
                "return ${clientClass.objectName}.request<${returnTypeName}, $typeWithoutOuterType>(${requestDataClass.objectName})" + nullableText
            }
        )
        .build()
}