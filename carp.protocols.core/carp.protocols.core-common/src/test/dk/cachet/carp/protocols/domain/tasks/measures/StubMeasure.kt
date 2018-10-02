package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure(
    @Serializable( DataTypeSerializer::class )
    override val type: DataType = StubDataType() ) : Measure()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubMeasure::class, "dk.cachet.carp.protocols.domain.tasks.measures.StubMeasure" ) }
    }
}