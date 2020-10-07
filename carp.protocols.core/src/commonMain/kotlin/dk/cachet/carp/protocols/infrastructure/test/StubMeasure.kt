package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure( override val type: DataType = STUB_DATA_TYPE, val uniqueProperty: String = "Unique" ) :
    Measure
