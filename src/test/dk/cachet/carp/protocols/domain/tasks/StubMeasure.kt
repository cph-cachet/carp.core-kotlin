package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.StubDataType
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure( override val type: DataType = StubDataType() ) : Measure()