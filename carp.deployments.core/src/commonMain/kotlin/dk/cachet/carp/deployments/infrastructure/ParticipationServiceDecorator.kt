package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData


class ParticipationServiceDecorator(
    service: ParticipationService,
    requestDecorator: (Command<ParticipationServiceRequest<*>>) -> Command<ParticipationServiceRequest<*>>
) : ApplicationServiceDecorator<ParticipationService, ParticipationServiceRequest<*>>(
        service,
        ParticipationServiceInvoker,
        requestDecorator
    ),
    ParticipationService
{
    override suspend fun getActiveParticipationInvitations(
        accountId: UUID
    ): Set<ActiveParticipationInvitation> = invoke(
        ParticipationServiceRequest.GetActiveParticipationInvitations( accountId )
    )

    override suspend fun getParticipantData( studyDeploymentId: UUID ): ParticipantData = invoke(
        ParticipationServiceRequest.GetParticipantData( studyDeploymentId )
    )

    override suspend fun getParticipantDataList( studyDeploymentIds: Set<UUID> ): List<ParticipantData> = invoke(
        ParticipationServiceRequest.GetParticipantDataList( studyDeploymentIds )
    )

    override suspend fun setParticipantData(
        studyDeploymentId: UUID,
        data: Map<InputDataType, Data?>,
        inputByParticipantRole: String?
    ): ParticipantData = invoke(
        ParticipationServiceRequest.SetParticipantData( studyDeploymentId, data, inputByParticipantRole )
    )
}


object ParticipationServiceInvoker : ApplicationServiceInvoker<ParticipationService, ParticipationServiceRequest<*>>
{
    override suspend fun ParticipationServiceRequest<*>.invoke( service: ParticipationService ): Any =
        when ( this )
        {
            is ParticipationServiceRequest.GetActiveParticipationInvitations ->
                service.getActiveParticipationInvitations( accountId )
            is ParticipationServiceRequest.GetParticipantData ->
                service.getParticipantData( studyDeploymentId )
            is ParticipationServiceRequest.GetParticipantDataList ->
                service.getParticipantDataList( studyDeploymentIds )
            is ParticipationServiceRequest.SetParticipantData ->
                service.setParticipantData( studyDeploymentId, data, inputByParticipantRole )
        }
}
