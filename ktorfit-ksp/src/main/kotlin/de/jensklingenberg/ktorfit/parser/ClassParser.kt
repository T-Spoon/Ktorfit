package de.jensklingenberg.ktorfit.parser

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ksp.toKModifier
import de.jensklingenberg.ktorfit.model.*
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.INTERFACE_NEEDS_TO_HAVE_A_PACKAGE
import de.jensklingenberg.ktorfit.model.KtorfitError.Companion.INTERNAL_INTERFACES_ARE_NOT_SUPPORTED
import de.jensklingenberg.ktorfit.utils.resolveTypeName
import java.io.File


/**
 * Gets the imports of a class by reading the imports from the file
 * which contains the class
 *  TODO: Find better way to get imports
 */
private fun getImports(ksClassDeclaration: KSClassDeclaration): List<String> {
    val importList =
        File(ksClassDeclaration.containingFile!!.filePath)
            .readLines()
            .filter { it.trimStart().startsWith("import") }
            .toMutableSet()

    importList.add(ktorfitClass.packageName + "." + ktorfitClass.name)
    importList.add("de.jensklingenberg.ktorfit.internal.*")

    return importList.map { it.removePrefix("import ") }
}

/**
 * Convert a [KSClassDeclaration] to [ClassData]
 * @param ksClassDeclaration interface that contains Ktorfit annotations
 * @param logger used to log errors
 * @return the transformed classdata
 */
fun toClassData(ksClassDeclaration: KSClassDeclaration, logger: KSPLogger): ClassData {

    val imports = getImports(ksClassDeclaration)
    val packageName = ksClassDeclaration.packageName.asString()
    val className = ksClassDeclaration.simpleName.asString()

    val isJavaClass = ksClassDeclaration.origin.name == "JAVA"
    if (isJavaClass) {
        logger.ktorfitError(KtorfitError.JAVA_INTERFACES_ARE_NOT_SUPPORTED, ksClassDeclaration)
    }

    val isInterface = ksClassDeclaration.classKind == ClassKind.INTERFACE
    if (!isInterface) {
        logger.ktorfitError(KtorfitError.API_DECLARATIONS_MUST_BE_INTERFACES, ksClassDeclaration)
    }

    val hasTypeParameters = ksClassDeclaration.typeParameters.isNotEmpty()
    if (hasTypeParameters) {
        logger.ktorfitError(
            KtorfitError.TYPE_PARAMETERS_ARE_UNSUPPORTED_ON + " ${ksClassDeclaration.simpleName.asString()}",
            ksClassDeclaration
        )
    }

    val functionDataList: List<FunctionData> =
        getFunctionDataList(ksClassDeclaration.getDeclaredFunctions().toList(), logger, imports, packageName)
    val supertypes =
        ksClassDeclaration.superTypes.toList().filterNot {
            /** In KSP Any is a supertype of an interface */
            it.resolve().resolveTypeName() == "Any"
        }.mapNotNull { it.resolve().declaration.qualifiedName?.asString() }
    val properties = ksClassDeclaration.getAllProperties().toList()

    if (packageName.isEmpty()) {
        logger.ktorfitError(INTERFACE_NEEDS_TO_HAVE_A_PACKAGE, ksClassDeclaration)
    }

    if (ksClassDeclaration.modifiers.contains(Modifier.INTERNAL)) {
        logger.ktorfitError(INTERNAL_INTERFACES_ARE_NOT_SUPPORTED, ksClassDeclaration)
    }

    return ClassData(
        className,
        packageName,
        functionDataList,
        imports,
        supertypes,
        properties,
        modifiers = ksClassDeclaration.modifiers.mapNotNull { it.toKModifier() })
}
