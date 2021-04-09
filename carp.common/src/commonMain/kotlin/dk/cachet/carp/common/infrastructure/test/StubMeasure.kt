package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.tasks.measures.Measure
import kotlinx.serialization.Serializable


@Serializable
data class StubMeasure( override val type: DataType = STUB_DATA_TYPE, val uniqueProperty: String = "Unique" ) :
    Measure
