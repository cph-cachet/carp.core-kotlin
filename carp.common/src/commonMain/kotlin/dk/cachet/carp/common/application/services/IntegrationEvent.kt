package dk.cachet.carp.common.application.services

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Required
import kotlin.js.JsExport


/**
 * An event raised by an application service [TApplicationService].
 *
 * Integration events need to be immutable.
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
@JsExport
interface IntegrationEvent<out TApplicationService : ApplicationService<out TApplicationService, *>>
{
    @Required
    val apiVersion: ApiVersion

    /**
     * All events related to the same aggregate ID are handled in order,
     * or no ordering is required in case `null`.
     *
     * In case the event pertains to an aggregate root,
     * specify its ID to ensure correct handling of business logic in the domain.
     */
    val aggregateId: String?
}
