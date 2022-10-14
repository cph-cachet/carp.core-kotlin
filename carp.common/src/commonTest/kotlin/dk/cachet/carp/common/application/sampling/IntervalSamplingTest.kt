@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds


/**
 * Tests for [IntervalSamplingScheme].
 */
class IntervalSamplingSchemeTest
{
    private val dataType = StubDataTypes.STUB_POINT

    @Test
    fun default_interval_needs_to_be_in_options()
    {
        val options = setOf( 100.milliseconds )
        assertFailsWith<IllegalArgumentException> { IntervalSamplingScheme( dataType, 50.milliseconds, options ) }
    }

    @Test
    fun at_least_one_option_needed()
    {
        assertFailsWith<IllegalArgumentException> { IntervalSamplingScheme( dataType, 50.milliseconds, emptySet() ) }
    }

    @Test
    fun isValid_true_when_no_constraints_are_set()
    {
        val noConstraints = IntervalSamplingScheme( dataType, 100.milliseconds )
        assertTrue( noConstraints.isValid( IntervalSamplingConfiguration( 0.milliseconds ) ) )
    }

    @Test
    fun isValid_true_when_value_in_options()
    {
        val options = setOf( 50.milliseconds, 100.milliseconds )
        val constrainedOptions = IntervalSamplingScheme( dataType, options.first(), options )

        options.forEach {
            assertTrue( constrainedOptions.isValid( IntervalSamplingConfiguration( it ) ) )
        }
    }

    @Test
    fun isValid_false_when_value_not_in_options()
    {
        val options = setOf( 50.milliseconds, 100.milliseconds )
        val constrainedOptions = IntervalSamplingScheme( dataType, options.first(), options )

        assertFalse( constrainedOptions.isValid( IntervalSamplingConfiguration( 200.milliseconds ) ) )
    }
}


/**
 * Tests for [IntervalSamplingConfigurationBuilder].
 */
class IntervalSamplingConfigurationBuilderTest
{
    @Test
    fun nearestOption_without_options()
    {
        val builder = IntervalSamplingConfigurationBuilder( 50.milliseconds, null )

        assertEquals( 42.milliseconds, builder.nearestOption( 42.milliseconds ) )
    }

    @Test
    fun nearestOption_selects_nearest_option_when_options_are_set()
    {
        val options = setOf( 50.milliseconds, 100.milliseconds )
        val builder = IntervalSamplingConfigurationBuilder( 50.milliseconds, options )

        assertEquals( 50.milliseconds, builder.nearestOption( 50.milliseconds ) )
        assertEquals( 50.milliseconds, builder.nearestOption( 0.milliseconds ) )
        assertEquals( 100.milliseconds, builder.nearestOption( 90.milliseconds ) )
    }
}
