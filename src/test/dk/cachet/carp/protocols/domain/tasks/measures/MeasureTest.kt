package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.StubDataType
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
        class NoDataClass( override val type: DataType ) : Measure()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass(StubDataType())
        }
    }
}