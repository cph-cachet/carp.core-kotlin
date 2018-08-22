package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.*
import kotlin.test.*


/**
 * Tests for [DataType].
 */
class DataTypeTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class NoDataClass( override val category: DataCategory = DataCategory.Other ) : DataType()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}