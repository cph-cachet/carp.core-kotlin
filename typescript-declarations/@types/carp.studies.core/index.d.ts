declare module 'carp.studies.core'
{
    import { dk as cdk } from 'carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import UUID = cdk.cachet.carp.common.UUID


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
        class StudyOwner
        {
            constructor( id?: UUID )

            static get Companion(): StudyOwner$Companion

            readonly id: UUID
        }
        interface StudyOwner$Companion { serializer(): any }
    }
}