declare module 'carp.core-kotlin-carp.protocols.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashSet = kotlin.collections.HashSet
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
    import Json = kotlinx.serialization.json.Json
    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import UUID = cdk.cachet.carp.common.UUID
    import ParticipantAttribute = cdk.cachet.carp.common.users.ParticipantAttribute


    namespace dk.cachet.carp.protocols.domain
    {
        namespace StudyProtocol
        {
            class Id
            {
                constructor( ownerId: UUID, name: string )

                static get Companion(): Id$Companion
    
                readonly ownerId: UUID
                readonly name: string
            }
            interface Id$Companion { serializer(): any }
        }

        class ProtocolOwner
        {
            constructor( id?: UUID )

            static get Companion(): ProtocolOwner$Companion

            readonly id: UUID
        }
        interface ProtocolOwner$Companion { serializer(): any }

        class ProtocolVersion
        {
            constructor( tag: string, date?: DateTime )

            static get Companion(): ProtocolVersion$Companion

            readonly tag: string
            readonly date: DateTime
        }
        interface ProtocolVersion$Companion { serializer(): any }

        class StudyProtocolSnapshot
        {
            // No manual initialization needed in TypeScript. Serialization should be used.
            private constructor()

            static get Companion(): StudyProtocolSnapshot$Companion

            readonly ownerId: UUID
            readonly name: string
            readonly description: string
            readonly creationDate: DateTime
            readonly expectedParticipantData: ArrayList<ParticipantAttribute>
        }
        interface StudyProtocolSnapshot$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.protocols.infrastructure
    {
        import ProtocolId = dk.cachet.carp.protocols.domain.StudyProtocol.Id
        import StudyProtocolSnapshot = dk.cachet.carp.protocols.domain.StudyProtocolSnapshot

        
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
            static Companion(): ProtocolFactoryServiceRequest$Companion
        }
        interface ProtocolFactoryServiceRequest$Companion { serializer(): any }

        namespace ProtocolFactoryServiceRequest
        {
            class CreateCustomProtocol extends ProtocolFactoryServiceRequest
            {
                constructor( ownerId: UUID, name: string, customProtocol: string, description: string )
            }
        }


        function createProtocolsSerializer_18xi4u$(): Json
    }
}
