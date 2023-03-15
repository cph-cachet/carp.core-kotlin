import VerifyModule from './VerifyModule'
import { expect } from 'chai'

import { dk as cdk } from '../src/carp-common'
import JSON = cdk.cachet.carp.common.infrastructure.serialization.JSON

import { dk } from '../src/carp-protocols-core'
import StudyProtocolSnapshot = dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import ProtocolServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"id":"ad4ca03a-6f69-4a95-8701-488dc511925b","createdOn":"2023-01-30T21:02:56.068710100Z","version":0,"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol","description":"Test description","primaryDevices":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration","isPrimaryDevice":true,"roleName":"Stub primary device"}],"connectedDevices":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration","roleName":"Stub device"},{"__type":"dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration","isPrimaryDevice":true,"roleName":"Chained primary"},{"__type":"dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration","roleName":"Chained connected"}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub primary device"},{"roleName":"Chained primary","connectedToRoleName":"Stub primary device"},{"roleName":"Chained connected","connectedToRoleName":"Chained primary"}],"tasks":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration","name":"Task","measures":[{"__type":"dk.cachet.carp.common.application.tasks.Measure.DataStream","type":"dk.cachet.carp.stubpoint"}]}],"triggers":{"0":{"__type":"dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration","sourceDeviceRoleName":"Stub device"}},"taskControls":[{"triggerId":0,"taskName":"Task","destinationDeviceRoleName":"Stub primary device","control":"Start"}],"participantRoles":[{"role":"Role","isOptional":false},{"role":"Optional role","isOptional":true}],"assignedDevices":{"Stub primary device":["Role"]},"expectedParticipantData":[{"attribute":{"__type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputDataType":"some.type"}},{"attribute":{"__type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputDataType":"dk.cachet.carp.input.sex"},"assignedTo":{"__type":"dk.cachet.carp.common.application.users.AssignedTo.Roles","roleNames":["Role"]}}]}`


describe( "carp.protocols.core", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = []

        const moduleVerifier = new VerifyModule(
            'carp.core-kotlin-carp.protocols.core',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "StudyProtocolSnapshot", () => {
        it( "can deserialize", () => {
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const parsed = JSON.decodeFromString( serializer, serializedSnapshot )
            expect( parsed ).is.instanceOf( StudyProtocolSnapshot )
        } )
    } )

    describe( "ProtocolServiceRequest", () => {
        it( "add request has default version tag", () => {
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const snapshot = JSON.decodeFromString( serializer, serializedSnapshot )

            const addProtocol = new ProtocolServiceRequest.Add( snapshot )
            const versionTag = (addProtocol as any).versionTag
            expect( versionTag ).equals( "Initial" )
        } )
    } )
} )
