import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
import Json = kotlinx.serialization.json.Json

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import StudyProtocolSnapshot = cdk.cachet.carp.common.application.StudyProtocolSnapshot
import createProtocolsSerializer = cdk.cachet.carp.common.infrastructure.serialization.createProtocolsSerializer_18xi4u$

import { dk } from 'carp.core-kotlin-carp.protocols.core'
import ProtocolVersion = dk.cachet.carp.protocols.application.ProtocolVersion
import ProtocolFactoryServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import ProtocolServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol","description":"Test description","creationDate":"2020-12-05T21:55:59.454Z","masterDevices":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Stub master device","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]}],"connectedDevices":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Stub device","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]},{"$type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Chained master","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]},{"$type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Chained connected","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub master device"},{"roleName":"Chained master","connectedToRoleName":"Stub master device"},{"roleName":"Chained connected","connectedToRoleName":"Chained master"}],"tasks":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor","name":"Task","measures":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubMeasure","type":"dk.cachet.carp.stub","uniqueProperty":"Unique"}]}],"triggers":{"0":{"$type":"dk.cachet.carp.common.infrastructure.test.StubTrigger","sourceDeviceRoleName":"Stub device","uniqueProperty":"Unique"}},"triggeredTasks":[{"triggerId":0,"taskName":"Task","targetDeviceRoleName":"Stub master device"}],"expectedParticipantData":[{"$type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputType":"some.type"}]}`


describe( "carp.protocols.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new ProtocolVersion( "Version" ),
            ProtocolVersion.Companion,
            ProtocolFactoryServiceRequest.Companion,
            ProtocolServiceRequest.Companion,
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.protocols.core', instances )
        await moduleVerifier.verify()
    } )

    describe( "ProtocolServiceRequest", () => {
        it( "add request has default version tag", () => {
            const json: Json = createProtocolsSerializer()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const snapshot = json.decodeFromString_awif5v$( serializer, serializedSnapshot )

            const addProtocol = new ProtocolServiceRequest.Add( snapshot )
            const versionTag = (addProtocol as any).versionTag
            expect( versionTag ).equals( "Initial" )
        } )
    })
} )
