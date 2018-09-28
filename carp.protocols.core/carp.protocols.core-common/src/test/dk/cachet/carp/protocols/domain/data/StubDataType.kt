package dk.cachet.carp.protocols.domain.data

import kotlinx.serialization.Serializable


@Serializable
data class StubDataType( override val category: DataCategory = DataCategory.Other ) : DataType()