package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.infrastructure.test.StubDataTypeSamplingScheme
import dk.cachet.carp.protocols.infrastructure.test.StubSamplingConfigurationBuilder
import dk.cachet.carp.test.JsIgnore
import kotlin.test.*


/**
 * Tests for [SamplingConfiguration].
 */
class SamplingConfigurationTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class HasVar( var notImmutable: String = "Mutable" ) : SamplingConfiguration()

        assertFailsWith<InvalidConfigurationError>
        {
            HasVar()
        }
    }
}


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
