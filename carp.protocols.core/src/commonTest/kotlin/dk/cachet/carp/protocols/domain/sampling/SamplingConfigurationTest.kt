@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.protocols.domain.sampling

import dk.cachet.carp.protocols.infrastructure.test.StubDataTypeSamplingScheme
import dk.cachet.carp.protocols.infrastructure.test.StubSamplingConfigurationBuilder
import kotlin.test.*


/**
 * Tests for [SamplingConfigurationMapBuilder].
 */
class SamplingConfigurationMapBuilderTest
{
    class TestBuilder : SamplingConfigurationMapBuilder()
    {
        fun someSampleConfiguration( builder: StubSamplingConfigurationBuilder.() -> Unit = { } ): SamplingConfiguration =
            addConfiguration( StubDataTypeSamplingScheme(), builder )
    }


    @Test
    fun addconfiguration_adds_samplingconfiguration()
    {
        val builder = TestBuilder()

        val configuration = builder.someSampleConfiguration()
        val configurations = builder.build()
        assertTrue( configuration in configurations.values )
    }
}
