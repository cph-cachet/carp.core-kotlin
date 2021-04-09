declare module 'carp.core-kotlin-carp.protocols.core'
{
    import { kotlin } from 'kotlin'
    import HashSet = kotlin.collections.HashSet

    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import DateTime = cdk.cachet.carp.common.application.DateTime
    import UUID = cdk.cachet.carp.common.application.UUID
    import ParticipantAttribute = cdk.cachet.carp.common.application.users.ParticipantAttribute


    namespace dk.cachet.carp.protocols.domain
    {
        class ProtocolVersion
        {
            constructor( tag: string, date?: DateTime )

            static get Companion(): ProtocolVersion$Companion

            readonly tag: string
            readonly date: DateTime
        }
        interface ProtocolVersion$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.protocols.infrastructure
    {
        import ProtocolId = cdk.cachet.carp.common.domain.StudyProtocol.Id
        import StudyProtocolSnapshot = cdk.cachet.carp.common.application.StudyProtocolSnapshot

        
        abstract class ProtocolServiceRequest
        {
            static get Companion(): ProtocolServiceRequest$Companion
        }
        interface ProtocolServiceRequest$Companion { serializer(): any }

        namespace ProtocolServiceRequest
        {
            class Add extends ProtocolServiceRequest
            {
                constructor( protocol: StudyProtocolSnapshot, versionTag?: string )
            }
            class AddVersion extends ProtocolServiceRequest
            {
                constructor( protocol: StudyProtocolSnapshot, versionTag?: string )
            }
            class UpdateParticipantDataConfiguration extends ProtocolServiceRequest
            {
                constructor( protocolId: ProtocolId, versionTag: string, expectedParticipantData: HashSet<ParticipantAttribute> )
            }
            class GetBy extends ProtocolServiceRequest
            {
                constructor( protocolId: ProtocolId, versionTag?: string )
            }
            class GetAllFor extends ProtocolServiceRequest
            {
                constructor( ownerId: UUID )
            }
            class GetVersionHistoryFor extends ProtocolServiceRequest
            {
                constructor( protocolId: ProtocolId )
            }
        }


        abstract class ProtocolFactoryServiceRequest
        {
            static get Companion(): ProtocolFactoryServiceRequest$Companion
        }
        interface ProtocolFactoryServiceRequest$Companion { serializer(): any }

        namespace ProtocolFactoryServiceRequest
        {
            class CreateCustomProtocol extends ProtocolFactoryServiceRequest
            {
                constructor( ownerId: UUID, name: string, customProtocol: string, description: string )
            }
        }
    }
}
