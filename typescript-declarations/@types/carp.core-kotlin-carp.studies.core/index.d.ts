declare module 'carp.core-kotlin-carp.studies.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashSet = kotlin.collections.HashSet
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-core-jsLegacy'
    import Json = kotlinx.serialization.json.Json
    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import EmailAddress = cdk.cachet.carp.common.EmailAddress
    import UUID = cdk.cachet.carp.common.UUID
    import AccountIdentity = cdk.cachet.carp.common.users.AccountIdentity
    import { dk as pdk } from 'carp.core-kotlin-carp.protocols.core'
    import StudyProtocolSnapshot = pdk.cachet.carp.protocols.domain.StudyProtocolSnapshot
    import { dk as ddk } from 'carp.core-kotlin-carp.deployment.core'
    import Participation = ddk.cachet.carp.deployment.domain.users.Participation
    import StudyDeploymentStatus = ddk.cachet.carp.deployment.domain.StudyDeploymentStatus
    import StudyInvitation = ddk.cachet.carp.deployment.domain.users.StudyInvitation


    namespace dk.cachet.carp.studies.domain
    {
        import StudyOwner = dk.cachet.carp.studies.domain.users.StudyOwner
        import DeanonymizedParticipation = dk.cachet.carp.studies.domain.users.DeanonymizedParticipation


        class ParticipantGroupStatus
        {
            constructor( studyDeploymentStatus: StudyDeploymentStatus, participants: HashSet<DeanonymizedParticipation> )

            static get Companion(): ParticipantGroupStatus$Companion

            readonly studyDeploymentStatus: StudyDeploymentStatus
            readonly participants: HashSet<DeanonymizedParticipation>
        }
        interface ParticipantGroupStatus$Companion { serializer(): any }


        class StudyDetails
        {
            constructor(
                studyId: UUID, studyOwner: StudyOwner, name: string, creationDate: DateTime,
                description: string,
                invitation: StudyInvitation,
                protocolSnapshot: StudyProtocolSnapshot | null )

            static get Companion(): StudyDetails$Companion

            readonly studyId: UUID
            readonly studyOwner: StudyOwner
            readonly name: string
            readonly creationDate: DateTime
            readonly description: string
            readonly invitation: StudyInvitation
            readonly protocolSnapshot: StudyProtocolSnapshot | null
        }
        interface StudyDetails$Companion { serializer(): any }


        abstract class StudyStatus
        {
            static get Companion(): StudyStatus$Companion
        }
        interface StudyStatus$Companion { serializer(): any }

        namespace StudyStatus
        {
            class Configuring
            {
                constructor(
                    studyId: UUID, name: string, creationDate: DateTime,
                    canSetInvitation: boolean,
                    canSetStudyProtocol: boolean,
                    canDeployToParticipants: boolean,
                    canGoLive: boolean )
    
                readonly studyId: UUID
                readonly name: string
                readonly creationDate: DateTime
                readonly canSetInvitation: boolean
                readonly canSetStudyProtocol: boolean
                readonly canDeployToParticipants: boolean
                readonly canGoLive: boolean
            }
            class Live
            {
                constructor(
                    studyId: UUID, name: string, creationDate: DateTime,
                    canSetInvitation: boolean,
                    canSetStudyProtocol: boolean,
                    canDeployToParticipants: boolean )
    
                readonly studyId: UUID
                readonly name: string
                readonly creationDate: DateTime
                readonly canSetInvitation: boolean
                readonly canSetStudyProtocol: boolean
                readonly canDeployToParticipants: boolean
            }
        }
    }


    namespace dk.cachet.carp.studies.domain.users
    {
        class AssignParticipantDevices
        {
            constructor( participantId: UUID, deviceRoleNames: HashSet<string> )

            static get Companion(): AssignParticipantDevices$Companion

            readonly participantId: UUID
            readonly deviceRoleNames: HashSet<string>
        }
        function participantIds_nvx6bb$( assignedGroup: ArrayList<AssignParticipantDevices> ): HashSet<UUID>
        function deviceRoles_nvx6bb$( assignedGroup: ArrayList<AssignParticipantDevices> ): HashSet<string>
        interface AssignParticipantDevices$Companion { serializer(): any }


        class DeanonymizedParticipation
        {
            constructor( participantId: UUID, participationId: UUID )

            static get Companion(): DeanonymizedParticipation$Companion

            readonly participantId: UUID
            readonly participationId: UUID
        }
        interface DeanonymizedParticipation$Companion { serializer(): any }


        class Participant
        {
            constructor( accountIdentity: AccountIdentity, id?: UUID )

            static get Companion(): Participant$Companion

            readonly accountIdentity: AccountIdentity
            readonly id: UUID
        }
        interface Participant$Companion { serializer(): any }


        class StudyOwner
        {
            constructor( id?: UUID )

            static get Companion(): StudyOwner$Companion

            readonly id: UUID
        }
        interface StudyOwner$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.studies.infrastructure
    {
        import AssignParticipantDevices = dk.cachet.carp.studies.domain.users.AssignParticipantDevices
        import StudyOwner = dk.cachet.carp.studies.domain.users.StudyOwner


        abstract class StudyServiceRequest
        {
            static get Companion(): StudyServiceRequest$Companion
        }
        interface StudyServiceRequest$Companion { serializer(): any }

        namespace StudyServiceRequest
        {
            class CreateStudy extends StudyServiceRequest
            {
                constructor( owner: StudyOwner, name: string, description: string, invitation: StudyInvitation )
            }
            class SetInternalDescription extends StudyServiceRequest
            {
                constructor( studyId: UUID, name: string, description: string )
            }
            class GetStudyDetails extends StudyServiceRequest
            {
                constructor( studyId: UUID )
            }
            class GetStudyStatus extends StudyServiceRequest
            {
                constructor( studyId: UUID )
            }
            class GetStudiesOverview extends StudyServiceRequest
            {
                constructor( owner: StudyOwner )
            }
            class AddParticipant extends StudyServiceRequest
            {
                constructor( studyId: UUID, email: EmailAddress )
            }
            class GetParticipants extends StudyServiceRequest
            {
                constructor( studyId: UUID )
            }
            class SetInvitation extends StudyServiceRequest
            {
                constructor( studyId: UUID, invitation: StudyInvitation )
            }
            class SetProtocol extends StudyServiceRequest
            {
                constructor( studyId: UUID, protocol: StudyProtocolSnapshot )
            }
            class GoLive extends StudyServiceRequest
            {
                constructor( studyId: UUID )
            }
            class DeployParticipantGroup extends StudyServiceRequest
            {
                constructor( studyId: UUID, group: HashSet<AssignParticipantDevices> )
            }
            class GetParticipantGroupStatusList extends StudyServiceRequest
            {
                constructor( studyId: UUID )
            }
            class StopParticipantGroup extends StudyServiceRequest
            {
                constructor( studyId: UUID, groupId: UUID )
            }
        }

        function createStudiesSerializer_18xi4u$(): Json
    }
}
