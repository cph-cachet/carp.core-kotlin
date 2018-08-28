package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.serialization.Serializable


@Serializable
data class StubDataType( override val category: DataCategory = DataCategory.Other ) : DataType()