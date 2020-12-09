declare module 'carp.core-kotlin-carp.protocols.core'
{
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
    import Json = kotlinx.serialization.json.Json
    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import DateTime = cdk.cachet.carp.common.DateTime
    import UUID = cdk.cachet.carp.common.UUID


    namespace dk.cachet.carp.protocols.domain
    {
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
                constructor( owner: ProtocolOwner, protocolName: string, versionTag?: string )
            }
            class GetAllFor extends ProtocolServiceRequest
            {
                constructor( owner: ProtocolOwner )
            }
            class GetVersionHistoryFor extends ProtocolServiceRequest
            {
                constructor( owner: ProtocolOwner, protocolName: string )
            }
        }

        function createProtocolsSerializer_18xi4u$(): Json
    }
}
