package de.jensklingenberg.ktorfit.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import de.jensklingenberg.ktorfit.model.ClassData
import java.io.OutputStreamWriter


/**
 * Generate the Impl class for every interface used for Ktorfit
 */
fun generateImplClass(classDataList: List<ClassData>, codeGenerator: CodeGenerator) {
    classDataList.forEach { classData ->
        val fileSource = getFileSource(classData)

        val packageName = classData.packageName
        val className = classData.name

        codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, "_${className}Impl", "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(fileSource)
            }
        }
    }
}

