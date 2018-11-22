package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.test.JsIgnore
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.data.*
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
        class NoDataClass( override val type: DataType ) : Measure()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass( STUB_DATA_TYPE )
        }
    }
}