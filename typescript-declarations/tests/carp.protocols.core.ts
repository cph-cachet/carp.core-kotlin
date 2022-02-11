import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import UUID = cdk.cachet.carp.common.application.UUID
import createDefaultJSON = cdk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$

import { dk } from 'carp.core-kotlin-carp.protocols.core'
import ProtocolVersion = dk.cachet.carp.protocols.application.ProtocolVersion
import StudyProtocolSnapshot = dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import ProtocolFactoryServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import ProtocolServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"id":"27879e75-ccc1-4866-9ab3-4ece1b735052","ownerId":"9586f572-58fd-11ec-bf63-0242ac130002","name":"Test protocol","description":"Test description","createdOn":"2021-06-18T14:28:14.229Z","masterDevices":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Stub master device","defaultSamplingConfiguration":{}}],"connectedDevices":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Stub device","defaultSamplingConfiguration":{}},{"__type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Chained master","defaultSamplingConfiguration":{}},{"__type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Chained connected","defaultSamplingConfiguration":{}}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub master device"},{"roleName":"Chained master","connectedToRoleName":"Stub master device"},{"roleName":"Chained connected","connectedToRoleName":"Chained master"}],"tasks":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor","name":"Task","measures":[{"__type":"dk.cachet.carp.common.application.tasks.Measure.DataStream","type":"dk.cachet.carp.stub","overrideSamplingConfiguration":null}],"description":null}],"triggers":{"0":{"__type":"dk.cachet.carp.common.infrastructure.test.StubTrigger","sourceDeviceRoleName":"Stub device","uniqueProperty":"Unique"}},"taskControls":[{"triggerId":0,"taskName":"Task","destinationDeviceRoleName":"Stub master device","control":"Start"}],"expectedParticipantData":[{"__type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputDataType":"some.type"}],"applicationData":""}`


describe( "carp.protocols.core", () => {
    it( "verify module declarations", async () => {
        // Create `StudyProtocolSnapshot` instance.
        const json: Json = createDefaultJSON()
        const serializer = StudyProtocolSnapshot.Companion.serializer()
        const studyProtocolSnapshot = json.decodeFromString_awif5v$( serializer, serializedSnapshot )

        const instances = [
            new ProtocolVersion( "Version" ),
            ProtocolVersion.Companion,
            studyProtocolSnapshot,
            StudyProtocolSnapshot.Companion,
            [ "ProtocolServiceRequest", new ProtocolServiceRequest.GetAllForOwner( UUID.Companion.randomUUID() ) ],
            [ "ProtocolFactoryServiceRequest", new ProtocolFactoryServiceRequest.CreateCustomProtocol( UUID.Companion.randomUUID(), "", "" ) ]
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.protocols.core', instances )
        await moduleVerifier.verify()
    } )

    describe( "StudyProtocolSnapshot", () => {
        it( "can deserialize", () => {
            const json: Json = createDefaultJSON()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const parsed = json.decodeFromString_awif5v$( serializer, serializedSnapshot )
            expect( parsed ).is.instanceOf( StudyProtocolSnapshot )
        } )
    } )

    describe( "ProtocolServiceRequest", () => {
        it( "add request has default version tag", () => {
            const json: Json = createDefaultJSON()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const snapshot = json.decodeFromString_awif5v$( serializer, serializedSnapshot )

            const addProtocol = new ProtocolServiceRequest.Add( snapshot )
            const versionTag = (addProtocol as any).versionTag
            expect( versionTag ).equals( "Initial" )
        } )
    })
} )
