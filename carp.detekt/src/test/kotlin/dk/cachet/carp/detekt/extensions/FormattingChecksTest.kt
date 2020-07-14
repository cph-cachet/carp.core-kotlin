package dk.cachet.carp.detekt.extensions

import io.github.detekt.test.utils.KtTestCompiler
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtObjectLiteralExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import kotlin.test.*


/**
 * Tests for functions defined in FormattingChecks.kt
 */
class FormattingChecksTest
{
    @Test
    fun isDefinedOnOneLine_succeeds()
    {
        val code =
            """
            package test
            fun oneline( code: Int ) = 42
            fun multiline( code: Int ) =
                42
            """
        val file = lint( code )
        val functions = file.children.filterIsInstance<KtFunction>()

        val oneLine = functions[ 0 ]
        assertTrue( isDefinedOnOneLine( oneLine ) )

        val multiline = functions[ 1 ]
        assertFalse( isDefinedOnOneLine( multiline ) )
    }

    @Test
    fun isDefinedOnOneLine_succeeds_for_first_line()
    {
        val oneLine = "package test"
        val packageDirective = lint( oneLine )
            .children.firstIsInstance<KtPackageDirective>()

        assertTrue( isDefinedOnOneLine( packageDirective ) )
    }

    @Test
    fun startsOnNewLine_succeeds_for_first_line()
    {
        val firstLine = "package test"
        val first = lint( firstLine ).firstChild

        assertTrue( startsOnNewLine( first ) )
    }

    @Test
    fun startsOnNewLine_works_with_trailing_spaces()
    {
        val trailingSpaceAfterPackage =
            """
            package test 
            import kotlin.test.*
            """
        val importList = lint( trailingSpaceAfterPackage )
            .children.firstIsInstance<KtImportList>()

        assertTrue( startsOnNewLine( importList ) )
    }

    @Test
    fun startsOnNewLine_works_for_elements_with_no_prev_sibling()
    {
        val noPrevSibling =
            """
            fun getObject() = object : Comparable<Int>
                {
                    override fun compareTo( other: Int ): Int = 0
                }
            """
        val objectDeclaration = lint( noPrevSibling )
            .children.firstIsInstance<KtFunction>()
            .children.firstIsInstance<KtObjectLiteralExpression>()
            .children.first() as KtObjectDeclaration

        assertFalse( startsOnNewLine( objectDeclaration ) )
    }

    @Test
    fun areAligned_succeeds()
    {
        val alignedCode =
            """
            package test
            
            fun one( code: Int ) = 42
            fun two( code: Int ) = 42
            """
        val aligned = lint( alignedCode )
            .children.filterIsInstance<KtFunction>()
        assertTrue( areAligned( aligned[ 0 ], aligned[ 1 ] ) )

        val notAlignedCode =
            """
            package test
            
            fun one( code: Int ) = 42
                fun two( code: Int ) = 42
            """
        val notAligned = lint( notAlignedCode )
            .children.filterIsInstance<KtFunction>()
        assertFalse( areAligned( notAligned[ 0 ], notAligned[ 1 ] ) )
    }

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

        assertTrue( areAligned( packageDirective, importList ) )
    }

    @Test
    fun areAligned_works_with_extra_newlines_and_spaces()
    {
        val alignedCode =
            """
            fun one( code: Int ) = 42
              
            fun two( code: Int ) = 42
            """
        val aligned = lint( alignedCode )
            .children.filterIsInstance<KtFunction>()
        assertTrue( areAligned( aligned[ 0 ], aligned[ 1 ] ) )
    }

    @Test
    fun getIndentSize_returns_zero_for_first_node()
    {
        val firstNode = "package test"
        val packageDirective = lint( firstNode )
            .children.firstIsInstance<KtPackageDirective>()
        assertEquals( 0, getIndentSize( packageDirective ) )
    }

    @Test
    fun getIndentSize_counts_spaces()
    {
        val oneSpaceCode = " package test"
        val oneSpace = KtTestCompiler.compileFromContent( oneSpaceCode ) // Do not trim indent.
            .children.firstIsInstance<KtPackageDirective>()
        assertEquals( 1, getIndentSize( oneSpace ) )

        val threeSpacesCode = "   package test"
        val threeSpaces = KtTestCompiler.compileFromContent( threeSpacesCode ) // Do not trim indent.
            .children.firstIsInstance<KtPackageDirective>()
        assertEquals( 3, getIndentSize( threeSpaces ) )
    }

    @Test
    fun getIndentSize_works_with_preceding_newline()
    {
        val code =
            """
            package test
            fun oneSpace( code: Int ) = 42
              fun twoSpaces( code: Int ) = 42
            """
        val functions = lint( code )
            .children.filterIsInstance<KtFunction>()

        val noSpace = functions[ 0 ]
        assertEquals( 0, getIndentSize( noSpace ) )

        val twoSpaces = functions[ 1 ]
        assertEquals( 2, getIndentSize( twoSpaces ) )
    }

    @Test
    fun getIndentSize_works_with_extra_newlines_and_spaces()
    {
        val code =
            """
            package test
              
            fun test( code: Int ) = 42
            """
        val function = lint( code )
            .children.firstIsInstance<KtFunction>()
        assertEquals( 0, getIndentSize( function ) )
    }

    @Test
    fun getIndentSize_works_with_preceding_elements()
    {
        val code = "package a fun test() = 42"
        val function = lint( code )
            .children.firstIsInstance<KtFunction>()
        assertEquals( 10, getIndentSize( function ) )
    }

    @Test
    fun getPrecedingElement_returns_null_for_first_node()
    {
        val first = "package test"
        val packageDirective = lint( first )
            .children.firstIsInstance<KtPackageDirective>()

        assertNull( getPrecedingElement( packageDirective ) )
    }

    @Test
    fun getNextElement_returns_null_for_last_node()
    {
        val last = "package test"
        val importList = lint( last )
            .children.firstIsInstance<KtImportList>()

        assertNull( getNextElement( importList ) )
    }


    private fun lint( code: String ): KtFile =
        KtTestCompiler.compileFromContent( code.trimIndent() )
}
