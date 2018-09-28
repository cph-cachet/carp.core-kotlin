package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.JsIgnore
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [Measure].
 */
class MeasureTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class NoDataClass(
            // TODO: Use the following serializer in JVM.
            //@Serializable( DataTypeSerializer::class )
            @Serializable( PolymorphicSerializer::class )
            override val type: DataType ) : Measure()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass(StubDataType())
        }
    }
}