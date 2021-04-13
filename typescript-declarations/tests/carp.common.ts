import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import { Long } from 'kotlin'
import toSet = kotlin.collections.toSet_us0mfu$

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
import Json = kotlinx.serialization.json.Json

import { dk } from 'carp.core-kotlin-carp.common'
import DateTime = dk.cachet.carp.common.application.DateTime
import EmailAddress = dk.cachet.carp.common.application.EmailAddress
import NamespacedId = dk.cachet.carp.common.application.NamespacedId
import StudyProtocolId = dk.cachet.carp.common.application.StudyProtocolId
import StudyProtocolSnapshot = dk.cachet.carp.common.application.StudyProtocolSnapshot
import TimeSpan = dk.cachet.carp.common.application.TimeSpan
import Trilean = dk.cachet.carp.common.application.Trilean
import UUID = dk.cachet.carp.common.application.UUID
import toTrilean = dk.cachet.carp.common.application.toTrilean_1v8dcc$
import DefaultDeviceRegistration = dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import SelectOne = dk.cachet.carp.common.application.data.input.elements.SelectOne
import Text = dk.cachet.carp.common.application.data.input.elements.Text
import DeviceRegistration = dk.cachet.carp.common.application.devices.DeviceRegistration
import AccountIdentity = dk.cachet.carp.common.application.users.AccountIdentity
import EmailAccountIdentity = dk.cachet.carp.common.application.users.EmailAccountIdentity
import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute
import UsernameAccountIdentity = dk.cachet.carp.common.application.users.UsernameAccountIdentity
import emailAccountIdentityFromString = dk.cachet.carp.common.application.users.EmailAccountIdentity_init_61zpoe$
import ProtocolOwner = dk.cachet.carp.common.domain.ProtocolOwner
import createDefaultJSON = dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$
import createProtocolsSerializer = dk.cachet.carp.common.infrastructure.serialization.createProtocolsSerializer_18xi4u$

const serializedSnapshot = `{"id":{"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol"},"description":"Test description","creationDate":"2020-12-05T21:55:59.454Z","masterDevices":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Stub master device","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]}],"connectedDevices":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Stub device","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]},{"$type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Chained master","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]},{"$type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Chained connected","samplingConfiguration":{},"supportedDataTypes":["dk.cachet.carp.stub"]}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub master device"},{"roleName":"Chained master","connectedToRoleName":"Stub master device"},{"roleName":"Chained connected","connectedToRoleName":"Chained master"}],"tasks":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor","name":"Task","measures":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubMeasure","type":"dk.cachet.carp.stub","uniqueProperty":"Unique"}]}],"triggers":{"0":{"$type":"dk.cachet.carp.common.infrastructure.test.StubTrigger","sourceDeviceRoleName":"Stub device","uniqueProperty":"Unique"}},"triggeredTasks":[{"triggerId":0,"taskName":"Task","targetDeviceRoleName":"Stub master device"}],"expectedParticipantData":[{"$type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputType":"some.type"}]}`


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        // Create `StudyProtocolSnapshot` instance.
        const json: Json = createProtocolsSerializer()
        const serializer = StudyProtocolSnapshot.Companion.serializer()
        const studyProtocolSnapshot = json.decodeFromString_awif5v$( serializer, serializedSnapshot )

        const instances = [
            DateTime.Companion.now(),
            DateTime.Companion,
            new EmailAddress( "test@test.com" ),
            EmailAddress.Companion,
            new NamespacedId( "namespace", "type" ),
            NamespacedId.Companion,
            studyProtocolSnapshot,
            StudyProtocolSnapshot.Companion,
            TimeSpan.Companion.INFINITE,
            TimeSpan.Companion,
            UUID.Companion.randomUUID(),
            UUID.Companion,
            [ "InputElement", new Text( "How are you feeling?" ) ],
            new SelectOne( "Sex", toSet( [ "Male", "Female" ] ) ),
            SelectOne.Companion,
            new Text( "How are you feeling?" ),
            Text.Companion,
            [ "DeviceRegistration", new DefaultDeviceRegistration( "some device id" ) ],
            DeviceRegistration.Companion,
            AccountIdentity.Factory,
            new EmailAccountIdentity( new EmailAddress( "test@test.com" ) ),
            EmailAccountIdentity.Companion,
            new UsernameAccountIdentity( "Test" ),
            UsernameAccountIdentity.Companion,
            [ "ParticipantAttribute", new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ) ],
            ParticipantAttribute.Companion,
            new ProtocolOwner(),
            ProtocolOwner.Companion,
            new StudyProtocolId( UUID.Companion.randomUUID(), "Name" ),
            StudyProtocolId.Companion,
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.common', instances )
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

    describe( "DateTime", () => {
        it( "serializes as string", () => {
            const dateTime = new DateTime( Long.fromNumber( 42 ) )
            
            const json: Json = createDefaultJSON()
            const serializer = DateTime.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, dateTime )
    
            expect( serialized ).equals( "\"1970-01-01T00:00:00.042Z\"" )
        } )
    
        it( "msSinceUTC is Long", () => {
            const now = DateTime.Companion.now()
    
            expect( now.msSinceUTC ).instanceOf( Long )
        } )
    } )


    describe( "TimeSpan", () => {
        it( "totalMilliseconds works", () => {
            const second = new TimeSpan( Long.fromNumber( 1000000 ) )
            const ms = second.totalMilliseconds
            expect( ms ).equals( 1000 )
        } )
    } )


    describe( "Trilean", () => {
        it( "has values TRUE, FALSE, UNKNOWN", () => {
            const values = Trilean.values()
            expect( values ).to.have.members( [ Trilean.TRUE, Trilean.FALSE, Trilean.UNKNOWN ] )
        } )

        it ( "toTrilean works", () => {
            expect( toTrilean( true ) ).equals( Trilean.TRUE )
            expect( toTrilean( false ) ).equals( Trilean.FALSE )
        } )
    } )


    describe( "EmailAccountIdentity", () => {
        it( "can initialize from string", () => {
            const identity = emailAccountIdentityFromString( "test@test.com" )
            expect( identity.emailAddress ).instanceOf( EmailAddress )
        } )
    } )

    describe( "ParticipantAttribute", () => {
        const attribute = new ParticipantAttribute.CustomParticipantAttribute( new Text( "Name" ) )

        it( "getInputElement works", () => {
            const inputElement = attribute.getInputElement_6eo89k$( CarpInputDataTypes )
            expect( inputElement ).instanceOf( Text )
        } )

        it( "isValidInput works", () => {
            const isNumberValid = attribute.isValidInput_etkzhw$( CarpInputDataTypes, 42 )
            expect( isNumberValid ).is.false

            const isStringValid = attribute.isValidInput_etkzhw$( CarpInputDataTypes, "Steven" )
            expect( isStringValid ).is.true
        } )

        it( "inputToData works", () => {
            const data = attribute.inputToData_etkzhw$( CarpInputDataTypes, "Steven" )
            expect( data ).is.not.undefined
        } )
    } )
} )
