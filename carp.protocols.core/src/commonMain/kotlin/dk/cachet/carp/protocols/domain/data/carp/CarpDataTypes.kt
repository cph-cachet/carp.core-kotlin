/**
 * Namespace which contains CARP data type definitions.
 */
package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.protocols.domain.data.DataType


/**
 * The [DataType] namespace of all CARP data type definitions.
 */
const val CARP_NAMESPACE: String = "dk.cachet.carp"

/**
 * Get a [DataType] definition with the given [name] in the [CARP_NAMESPACE].
 */
internal fun carpDataType( name: String ) = DataType( CARP_NAMESPACE, name )
