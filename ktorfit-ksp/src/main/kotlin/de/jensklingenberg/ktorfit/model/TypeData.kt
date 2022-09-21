package de.jensklingenberg.ktorfit.model

import de.jensklingenberg.ktorfit.utils.surroundIfNotEmpty



data class TypeData(val qualifiedName: String, val typeArgs: List<TypeData> = emptyList()) {
    override fun toString(): String {
        val typeArgumentsText = typeArgs.joinToString { it.toString() }.surroundIfNotEmpty(",listOf(", ")")
        return """TypeData("$qualifiedName"$typeArgumentsText)"""
    }
}

