declare module 'carp.studies.core'
{
    import { dk as cdk } from 'carp.common'
    import UUID = cdk.cachet.carp.common.UUID


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