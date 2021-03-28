package dk.cachet.carp.common.application.data.input.element

import kotlin.test.*


/**
 * Tests for [SelectOne].
 */
class SelectOneTest
{
    @Test
    fun isValid_is_true_for_listed_options()
    {
        val selection = SelectOne( "Sex", setOf( "Male", "Female" ) )
        assertTrue( selection.isValid( selection.options.first() ) )
    }

    @Test
    fun isValid_is_false_for_unlisted_options()
    {
        val selection = SelectOne( "Sex", setOf( "Male", "Female" ) )
        assertFalse( selection.isValid( "Zorg" ) )
    }

    @Test
    fun at_least_one_option_needed()
    {
        assertFailsWith<IllegalArgumentException> { SelectOne( "None", emptySet() ) }
    }
}
