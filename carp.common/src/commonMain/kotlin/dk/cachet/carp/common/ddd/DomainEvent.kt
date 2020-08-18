package dk.cachet.carp.common.ddd

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.ImplementAsDataClass
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * A domain event raised by an [AggregateRoot].
 *
 * Domain events need to be immutable.
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
abstract class DomainEvent
