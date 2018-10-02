package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubDataType( override val category: DataCategory = DataCategory.Other ) : DataType()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubDataType::class, "dk.cachet.carp.protocols.domain.data.StubDataType" ) }
    }
}