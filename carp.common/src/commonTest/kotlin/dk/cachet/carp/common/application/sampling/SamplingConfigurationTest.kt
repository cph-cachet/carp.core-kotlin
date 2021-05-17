@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.infrastructure.test.StubDataTypeSamplingScheme
import kotlin.test.*


/**
 * Tests for [SamplingConfigurationMapBuilder].
 */
class SamplingConfigurationMapBuilderTest
{
    class TestBuilder : SamplingConfigurationMapBuilder()
    {
        fun someSampleConfiguration( builder: NoOptionsSamplingConfigurationBuilder.() -> Unit = { } ): SamplingConfiguration =
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
