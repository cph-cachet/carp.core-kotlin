package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure( override val type: DataType = STUB_DATA_TYPE ) : Measure()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StubMeasure::class,
                StubMeasure.serializer(),
                "dk.cachet.carp.protocols.domain.tasks.measures.StubMeasure" )
        }
    }
}