package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure(
    @Serializable( DataTypeSerializer::class )
    override val type: DataType = StubDataType() ) : Measure()