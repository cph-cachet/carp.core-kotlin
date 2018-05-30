package dk.cachet.carp.protocols.domain.data

import kotlinx.serialization.Serializable


@Serializable
data class StubDataType( val descriptor: String = "Test" ) : DataType()