package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Polymorphic


/**
 * A domain event raised by an [AggregateRoot].
 *
 * Domain events need to be immutable.
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface DomainEvent
