package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure(
    @Serializable( with = DataTypeSerializer::class )
    override val type: DataType = StubDataType() ) : Measure()