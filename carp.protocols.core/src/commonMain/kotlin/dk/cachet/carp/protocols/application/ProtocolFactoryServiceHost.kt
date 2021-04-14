package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.CustomProtocolDevice
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Implementation of [ProtocolFactoryService] which provides factory methods
 * to create [StudyProtocolSnapshot]'s according to predefined templates.
*/
class ProtocolFactoryServiceHost : ProtocolFactoryService
{
    /**
     * Create a study protocol to be deployed to a single device which has its own way of describing study protocols that
     * deviates from the CARP core study protocol model.
     *
     * The [customProtocol] is stored in a single [CustomProtocolTask] which in the CARP study protocol model is described
     * as being triggered at the start of the study for a [CustomProtocolDevice] with role name "Custom device".
     */
    override suspend fun createCustomProtocol(
        ownerId: UUID,
        name: String,
        customProtocol: String,
        description: String
    ): StudyProtocolSnapshot
    {
        // Get protocol owner.
        // TODO: If `ProtocolOwner` ever takes additional fields this needs to be retrieved from the repository.
        val owner = ProtocolOwner( ownerId )

        val protocol = StudyProtocol( owner, name, description )
        val customDevice = CustomProtocolDevice( "Custom device" )
        protocol.addMasterDevice( customDevice )
        val task = CustomProtocolTask( "Custom device task", customProtocol )
        protocol.addTriggeredTask( customDevice.atStartOfStudy(), task, customDevice )

        return protocol.getSnapshot()
    }
}
