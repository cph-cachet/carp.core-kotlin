import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
import Json = kotlinx.serialization.json.Json
import { dk as dkc } from 'carp.core-kotlin-carp.common'
import UUID = dkc.cachet.carp.common.UUID
import { dk } from 'carp.core-kotlin-carp.protocols.core'
import ProtocolId = dk.cachet.carp.protocols.domain.StudyProtocol.Id
import ProtocolOwner = dk.cachet.carp.protocols.domain.ProtocolOwner
import ProtocolVersion = dk.cachet.carp.protocols.domain.ProtocolVersion
import StudyProtocolSnapshot = dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import createProtocolsSerializer = dk.cachet.carp.protocols.infrastructure.createProtocolsSerializer_18xi4u$
import ProtocolServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol","description":"Test description","creationDate":"2020-12-05T21:55:59.454Z","masterDevices":[{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Stub master device","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]}],"connectedDevices":[{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor","roleName":"Stub device","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]},{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Chained master","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]},{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor","roleName":"Chained connected","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub master device"},{"roleName":"Chained master","connectedToRoleName":"Stub master device"},{"roleName":"Chained connected","connectedToRoleName":"Chained master"}],"tasks":[{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor","name":"Task","measures":[{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubMeasure","type":"dk.cachet.carp.stub","uniqueProperty":"Unique"}]}],"triggers":{"0":{"$type":"dk.cachet.carp.protocols.infrastructure.test.StubTrigger","sourceDeviceRoleName":"Stub device","uniqueProperty":"Unique"}},"triggeredTasks":[{"triggerId":0,"taskName":"Task","targetDeviceRoleName":"Stub master device"}],"expectedParticipantData":[{"$type":"dk.cachet.carp.common.users.ParticipantAttribute.DefaultParticipantAttribute","inputType":"some.type"}]}`


describe( "carp.protocols.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new ProtocolId( UUID.Companion.randomUUID(), "Name" ),
            [ "Id$Companion", ProtocolId.Companion ],
            StudyProtocolSnapshot.Companion,
            ProtocolVersion.Companion,
            ProtocolServiceRequest.Companion,
            ProtocolOwner.Companion
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.protocols.core', instances )
        await moduleVerifier.verify()
    } )

    describe( "StudyProtocolSnapshot", () => {
        it( "can deserialize", () => {
            const json: Json = createProtocolsSerializer()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const parsed = json.decodeFromString_awif5v$( serializer, serializedSnapshot )
            expect( parsed ).is.instanceOf( StudyProtocolSnapshot )
        } )
    } )

    describe( "ProtocolServiceRequest", () => {
        it( "add request has default version tag", () => {
            const addProtocol = new ProtocolServiceRequest.Add( serializedSnapshot )
            const versionTag = (addProtocol as any).versionTag
            expect( versionTag ).equals( "Initial" )
        } )
    })
} )
