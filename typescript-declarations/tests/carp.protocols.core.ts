import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-runtime'
import Json = kotlinx.serialization.json.Json
import { dk } from 'carp.protocols.core'
import ProtocolOwner = dk.cachet.carp.protocols.domain.ProtocolOwner
import ProtocolVersion = dk.cachet.carp.protocols.domain.ProtocolVersion
import StudyProtocolSnapshot = dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import createProtocolsSerializer = dk.cachet.carp.protocols.infrastructure.createProtocolsSerializer_stpyu4$
import ProtocolServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol","description":"","creationDate":1587991653533,"masterDevices":[["dk.cachet.carp.protocols.domain.devices.StubMasterDeviceDescriptor",{"isMasterDevice":true,"roleName":"Stub master device"}]],"connectedDevices":[["dk.cachet.carp.protocols.domain.devices.StubDeviceDescriptor",{"roleName":"Stub device"}],["dk.cachet.carp.protocols.domain.devices.StubMasterDeviceDescriptor",{"isMasterDevice":true,"roleName":"Chained master"}],["dk.cachet.carp.protocols.domain.devices.StubDeviceDescriptor",{"roleName":"Chained connected"}]],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub master device"},{"roleName":"Chained master","connectedToRoleName":"Stub master device"},{"roleName":"Chained connected","connectedToRoleName":"Chained master"}],"tasks":[["dk.cachet.carp.protocols.domain.tasks.StubTaskDescriptor",{"name":"Task","measures":[["dk.cachet.carp.protocols.domain.tasks.measures.StubMeasure",{"type":{"namespace":"dk.cachet.carp","name":"stub"}}]]}]],"triggers":{"0":["dk.cachet.carp.protocols.domain.triggers.StubTrigger",{"sourceDeviceRoleName":"Stub device","uniqueProperty":"Unique"}]},"triggeredTasks":[{"triggerId":0,"taskName":"Task","targetDeviceRoleName":"Stub master device"}]}`


describe( "carp.protocols.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            StudyProtocolSnapshot.Companion,
            ProtocolVersion.Companion,
            ProtocolServiceRequest.Companion,
            ProtocolOwner.Companion
        ]

        const moduleVerifier = new VerifyModule( 'carp.protocols.core', instances )
        await moduleVerifier.verify()
    } )

    describe( "StudyProtocolSnapshot", () => {
        it( "can deserialize", () => {
            const json: Json = createProtocolsSerializer()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const parsed = json.parse_awif5v$( serializer, serializedSnapshot )
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
