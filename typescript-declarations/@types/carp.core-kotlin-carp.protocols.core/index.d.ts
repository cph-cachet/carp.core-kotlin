declare module 'carp.core-kotlin-carp.protocols.core'
{
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
    import Json = kotlinx.serialization.json.Json
    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import UUID = cdk.cachet.carp.common.UUID


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
        }
        interface ProtocolOwner$Companion { serializer(): any }

        class ProtocolVersion
        {
            constructor( date: DateTime, tag: string )

            static get Companion(): ProtocolVersion$Companion
        }
        interface ProtocolVersion$Companion { serializer(): any }

        class StudyProtocolSnapshot
        {
            // No manual initialization needed in TypeScript. Serialization should be used.
            private constructor()

            static get Companion(): StudyProtocolSnapshot$Companion
        }
        interface StudyProtocolSnapshot$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.protocols.infrastructure
    {
        import ProtocolId = dk.cachet.carp.protocols.domain.StudyProtocol.Id
        import ProtocolOwner = dk.cachet.carp.protocols.domain.ProtocolOwner
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

        function createProtocolsSerializer_18xi4u$(): Json
    }
}
