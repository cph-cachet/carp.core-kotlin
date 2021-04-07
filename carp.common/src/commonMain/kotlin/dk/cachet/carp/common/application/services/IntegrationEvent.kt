package dk.cachet.carp.common.application.services

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * An event raised by an application service [TApplicationService].
 *
 * Integration events need to be immutable.
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
abstract class IntegrationEvent<out TApplicationService : ApplicationService<out TApplicationService, *>>
