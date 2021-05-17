package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
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
