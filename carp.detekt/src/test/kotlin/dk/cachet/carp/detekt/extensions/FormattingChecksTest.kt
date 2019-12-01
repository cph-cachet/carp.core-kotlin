package dk.cachet.carp.detekt.extensions

import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import kotlin.test.*


/**
 * Tests for functions defined in FormattingChecks.kt
 */
class FormattingChecksTest
{
    @Test
    fun areAligned_works_for_first_node_in_code()
    {
        val aligned =
            """
            package test
            import kotlin.test.*
            """
        val file = lint( aligned )
        val packageDirective = file.children.firstIsInstance<KtPackageDirective>()
        val importList = file.children.firstIsInstance<KtImportList>()

        assertTrue( areAligned( packageDirective.node, importList.node ) )
    }

    @Test
    fun startsOnNewLine_works_with_trailing_spaces()
    {
        val trailingSpaceAfterPackage =
            """
            package test 
            import kotlin.test.*
            """
        val file = lint( trailingSpaceAfterPackage )
        val importList = file.children.firstIsInstance<KtImportList>()

        assertTrue( startsOnNewLine( importList.node ) )
    }

    @Test
    fun getPrecedingNode_returns_null_for_first_node()
    {
        val aligned = "package test"
        val file = lint( aligned )
        val packageDirective = file.children.firstIsInstance<KtPackageDirective>()

        assertNull( getPrecedingNode( packageDirective.node ) )
    }


    private fun lint( code: String ): KtFile =
        KtTestCompiler.compileFromContent( code.trimIndent() )
}
