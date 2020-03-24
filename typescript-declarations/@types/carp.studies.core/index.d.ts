declare module 'carp.studies.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashSet = kotlin.collections.HashSet
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-runtime'
    import Json = kotlinx.serialization.json.Json
    import { dk as cdk } from 'carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import EmailAddress = cdk.cachet.carp.common.EmailAddress
    import UUID = cdk.cachet.carp.common.UUID
    import AccountIdentity = cdk.cachet.carp.common.users.AccountIdentity
    import { dk as pdk } from 'carp.protocols.core'
    import StudyProtocolSnapshot = pdk.cachet.carp.protocols.domain.StudyProtocolSnapshot
    import { dk as ddk } from 'carp.deployment.core'
    import StudyInvitation = ddk.cachet.carp.deployment.domain.users.StudyInvitation


    namespace dk.cachet.carp.studies.domain
    {
        import StudyOwner = dk.cachet.carp.studies.domain.users.StudyOwner

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
                    canDeployToParticipants: boolean,
                    canSetStudyProtocol: boolean,
                    canGoLive: boolean )
    
                readonly studyId: UUID
                readonly name: string
                readonly creationDate: DateTime
                readonly canDeployToParticipants: boolean
                readonly canSetStudyProtocol: boolean
                readonly canGoLive: boolean
            }
            class Live
            {
                constructor(
                    studyId: UUID, name: string, creationDate: DateTime,
                    canDeployToParticipants: boolean,
                    canSetStudyProtocol: boolean )
    
                readonly studyId: UUID
                readonly name: string
                readonly creationDate: DateTime
                readonly canDeployToParticipants: boolean
                readonly canSetStudyProtocol: boolean
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
            class UpdateInternalDescription extends StudyServiceRequest
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
        }

        function createStudiesSerializer_stpyu4$(): Json
    }
}
