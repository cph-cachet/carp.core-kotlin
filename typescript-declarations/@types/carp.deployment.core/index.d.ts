declare module 'carp.deployment.core'
{
    import { dk as cdk } from 'carp.common'
    import UUID = cdk.cachet.carp.common.UUID


    namespace dk.cachet.carp.deployment.domain.users
    {
        class StudyInvitation
        {
            constructor( name: string )

            static get Companion(): StudyInvitation$Companion

            readonly name: UUID
        }
        interface StudyInvitation$Companion
        {
            serializer(): any;
            empty(): StudyInvitation;
        }
    }
}