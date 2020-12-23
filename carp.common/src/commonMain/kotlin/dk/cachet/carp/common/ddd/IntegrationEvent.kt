package dk.cachet.carp.common.ddd

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.ImplementAsDataClass
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * An event raised by an application service.
 *
 * Integration events need to be immutable.
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
abstract class IntegrationEvent
