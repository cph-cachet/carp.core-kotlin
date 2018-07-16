package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertFailsWith


/**
 * Tests for [Measure].
 */
class MeasureTest
{
    @Test
    fun `mutable implementation triggers exception`()
    {
        class NoDataClass(
            @Serializable( with = DataTypeSerializer::class )
            override val type: DataType ) : Measure()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass(StubDataType())
        }
    }
}