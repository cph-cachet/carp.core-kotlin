declare module 'carp.studies.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashSet = kotlin.collections.HashSet
    import { dk as cdk } from 'carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import UUID = cdk.cachet.carp.common.UUID
    import AccountIdentity = cdk.cachet.carp.common.users.AccountIdentity


    namespace dk.cachet.carp.studies.domain
    {
        class StudyStatus
        {
            constructor( studyId: UUID, name: string, creationDate: DateTime, canDeployToParticipants: boolean, isLive: boolean )

            static get Companion(): StudyStatus$Companion

            readonly studyId: UUID
            readonly name: string
            readonly creationDate: DateTime
            readonly canDeployToParticipants: boolean
            readonly isLive: boolean
        }
        interface StudyStatus$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.studies.domain.users
    {
        class AssignParticipantDevices
        {
            constructor( participantId: UUID, deviceRoleNames: HashSet<string> )

            static get Companion(): AssignParticipantDevices$Companion

            readonly participantId: UUID
            readonly deviceRoleNames: Set<string>
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
}