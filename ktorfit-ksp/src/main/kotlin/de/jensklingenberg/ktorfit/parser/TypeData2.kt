package de.jensklingenberg.ktorfit.parser

import KtorfitProcessor
import com.google.devtools.ksp.getClassDeclarationByName

//https://kotlinlang.org/docs/packages.html
fun defaultImports() = listOf(
    "kotlin.*",
    "kotlin.annotation.*",
    "kotlin.collections.*",
    "kotlin.comparisons.*",
    "kotlin.io.*",
    "kotlin.ranges.*",
    "kotlin.sequences.*",
    "kotlin.text.*"
)

data class TypeData2(val qualifiedName: String, val typeArgs: List<TypeData2> = emptyList()) {
    override fun toString(): String {
        var qua = qualifiedName
        val args = typeArgs.joinToString() { it.toString() }
        val args2 = if (args.isNotEmpty()) {

            "listOf($args)"
        } else {
            ""
        }
        return """TypeData("$qua",$args2)"""
    }
}


fun getMyType(text: String, imports: List<String>, packageName: String): TypeData2 {
    val classImports = imports + defaultImports()
    var className = text.substringBefore("<", "")
    if (className.isEmpty()) {
        className = text.substringBefore(",", "")
    }
    if (className.isEmpty()) {
        className = text
    }
    val type = (text.removePrefix(className)).substringAfter("<").substringBeforeLast(">")
    val argumentsTypes = mutableListOf<TypeData2>()
    if (type.contains("<")) {
        argumentsTypes.add(getMyType(type, classImports, packageName))
    } else if (type.contains(",")) {
        type.split(",").forEach {
            argumentsTypes.add(getMyType(it, classImports, packageName))
        }
    } else if (type.isNotEmpty()) {
        argumentsTypes.add(getMyType(type, classImports, packageName))
    }


    //Look in package
    val found =
        KtorfitProcessor.rresolver.getClassDeclarationByName("$packageName.$className")?.qualifiedName?.asString()
    found?.let {
        className = it
    }

    //Look in imports


    //Wildcards
    val isWildCard = className == "*"
    if (!isWildCard) {
        classImports.forEach {
            if (it.substringAfterLast(".") == className) {
                className = it
            }

            val packageName = it.substringBeforeLast(".")
            val found2 =
                KtorfitProcessor.rresolver.getClassDeclarationByName("$packageName.$className")?.qualifiedName?.asString()
            found2?.let {
                className = it
            }
        }
    }
    val nullable = if (text.endsWith("?")) {
        "?"
    } else {
        ""
    }

    return TypeData2(className + nullable, argumentsTypes)
}