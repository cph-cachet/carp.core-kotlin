package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.STUB_DATA_TYPE
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure( override val type: DataType = STUB_DATA_TYPE ) : Measure()
