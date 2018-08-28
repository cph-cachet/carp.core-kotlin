package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.serialization.*


@Serializable
data class StubMeasure(
    @SerializableWith( DataTypeSerializer::class )
    override val type: DataType = StubDataType() ) : Measure()