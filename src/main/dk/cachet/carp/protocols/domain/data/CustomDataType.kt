package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper


/**
 * A wrapper used to load extending types from [DataType]s serialized as JSON which are unknown at runtime.
 */
data class CustomDataType( override val className: String, override val jsonSource: String ) : DataType(), UnknownPolymorphicWrapper