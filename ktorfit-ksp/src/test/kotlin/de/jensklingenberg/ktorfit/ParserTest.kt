package de.jensklingenberg.ktorfit

import com.google.common.truth.Truth
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import de.jensklingenberg.ktorfit.model.getMyType
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class ParserTest {

    @Test
    fun tes2t() {
        val name = object :KSName{
            override fun asString(): String {

            }

            override fun getQualifier(): String {
                TODO("Not yet implemented")
            }

            override fun getShortName(): String {
                TODO("Not yet implemented")
            }

        }
        val mockDec = mock<KSClassDeclaration>()
        Mockito.`when`(mockDec.qualifiedName).doReturn("")
        val mockResolver = mock<Resolver>()
        val test = getMyType("Map<String,Int>", emptyList(),"com.example.api",mockResolver)
        Truth.assertThat(test.qualifiedName).isEqualTo("Map")
    }
}