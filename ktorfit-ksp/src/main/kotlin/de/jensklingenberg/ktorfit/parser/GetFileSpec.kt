package de.jensklingenberg.ktorfit.parser

import com.squareup.kotlinpoet.*
import de.jensklingenberg.ktorfit.model.ClassData
import de.jensklingenberg.ktorfit.model.clientClass
import de.jensklingenberg.ktorfit.model.ktorfitClass
import de.jensklingenberg.ktorfit.utils.resolveTypeName
private const val WILDCARDIMPORT = "WILDCARDIMPORT"

/**
 * Transform a [ClassData] to a [FileSpec] for KotlinPoet
 */
fun getImplClassFileSource(classData: ClassData): String {

    /**
     * public fun Ktorfit.createExampleApi(): ExampleApi = _ExampleApiImpl(KtorfitClient(this))
     */
    val createExtensionFunctionSpec = FunSpec.builder("create${classData.name}")
        .addStatement("return _${classData.name}Impl(${clientClass.name}(this))")
        .receiver(TypeVariableName(ktorfitClass.name))
        .returns(TypeVariableName(classData.name))
        .build()

    val properties = classData.properties.map { property ->
        val propBuilder = PropertySpec.builder(
            property.simpleName.asString(),
            TypeVariableName(property.type.resolve().resolveTypeName())
        )
            .addModifiers(KModifier.OVERRIDE)
            .mutable(property.isMutable)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("TODO(\"Not yet implemented\")")
                    .build()
            )

        if (property.isMutable) {
            propBuilder.setter(
                FunSpec.setterBuilder()
                    .addParameter("value", TypeVariableName(property.type.resolve().resolveTypeName()))
                    .build()
            )
        }

        propBuilder.build()
    }

    val implClassName = "_${classData.name}Impl"

    return FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated by Ktorfit")
        .addImports(classData.imports)
        .addType(
            TypeSpec.classBuilder(implClassName)
                .addModifiers(classData.modifiers)
                .addSuperinterface(ClassName(classData.packageName, classData.name))
                .addKtorfitSuperInterface(classData.superClasses)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(clientClass.objectName, TypeVariableName(clientClass.name))
                        .build()
                )
                .addProperty(
                    PropertySpec.builder(clientClass.objectName, TypeVariableName(clientClass.name))
                        .addModifiers(KModifier.PRIVATE)
                        .initializer(clientClass.objectName)
                        .build()
                )
                .addFunctions(getFunSpecs(classData))
                .addProperties(properties)
                .build()
        )
        .addFunction(createExtensionFunctionSpec)

        .build().toString().replace(WILDCARDIMPORT, "*")
}


/**
 * Support for extending multiple interfaces, is done with Kotlin delegation. Ktorfit interfaces can only extend other Ktorfit interfaces, so there will
 * be a generated implementation for each interface that we can use.
 */
fun TypeSpec.Builder.addKtorfitSuperInterface(superClasses: List<String>): TypeSpec.Builder {
    superClasses.forEach { superClassQualifiedName ->
        val superTypeClassName = superClassQualifiedName.substringAfterLast(".")
        val superTypePackage = superClassQualifiedName.substringBeforeLast(".")
        this.addSuperinterface(
            ClassName(superTypePackage, superTypeClassName),
            CodeBlock.of("${superTypePackage}._${superTypeClassName}Impl(${clientClass.objectName})")
        )
    }

    return this
}


fun FileSpec.Builder.addImports(imports: List<String>): FileSpec.Builder {

    imports.forEach {
        /**
         * Wildcard imports are not allowed by KotlinPoet, as a workaround * is replaced with WILDCARDIMPORT, and it will be replaced again
         * after Kotlin Poet generated the source code
         */
        val packageName = it.substringBeforeLast(".")
        val className = it.substringAfterLast(".").replace("*", WILDCARDIMPORT)

        this.addImport(packageName, className)
    }
    return this
}