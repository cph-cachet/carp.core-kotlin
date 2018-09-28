package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure(
    // TODO: Use the following serializer in JVM.
    //@Serializable( DataTypeSerializer::class )
    @Serializable( PolymorphicSerializer::class )
    override val type: DataType = StubDataType() ) : Measure()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubMeasure::class, "dk.cachet.carp.protocols.domain.tasks.measures.StubMeasure" ) }
    }
}