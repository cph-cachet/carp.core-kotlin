package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.CustomProtocolDevice
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import kotlinx.serialization.Serializable


/**
 * Factory methods to create [StudyProtocolSnapshot]'s according to predefined templates.
 */
@ApiVersion( 1, 0 )
interface ProtocolFactoryService : ApplicationService<ProtocolFactoryService, ProtocolFactoryService.Event>
{
    @Serializable
    sealed class Event : IntegrationEvent<ProtocolFactoryService>


    /**
     * Create a study protocol to be deployed to a single device which has its own way of describing study protocols that
     * deviates from the CARP core study protocol model.
     *
     * The [customProtocol] is stored in a single [CustomProtocolTask] which in the CARP study protocol model is described
     * as being triggered at the start of the study for a [CustomProtocolDevice] with role name "Custom device".
     */
    suspend fun createCustomProtocol(
        ownerId: UUID,
        name: String,
        customProtocol: String,
        description: String? = null
    ): StudyProtocolSnapshot
}
