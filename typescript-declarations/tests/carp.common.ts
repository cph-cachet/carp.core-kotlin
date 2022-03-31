import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import toSet = kotlin.collections.toSet_us0mfu$;
import Duration = kotlin.time.Duration;

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json

import { dk } from 'carp.core-kotlin-carp.common'
import EmailAddress = dk.cachet.carp.common.application.EmailAddress;
import NamespacedId = dk.cachet.carp.common.application.NamespacedId;
import RecurrenceRule = dk.cachet.carp.common.application.RecurrenceRule;
import TimeOfDay = dk.cachet.carp.common.application.TimeOfDay;
import Trilean = dk.cachet.carp.common.application.Trilean;
import UUID = dk.cachet.carp.common.application.UUID;
import toTrilean = dk.cachet.carp.common.application.toTrilean_1v8dcc$;
import DefaultDeviceRegistration = dk.cachet.carp.common.application.devices.DefaultDeviceRegistration;
import DeviceRegistration = dk.cachet.carp.common.application.devices.DeviceRegistration;
import Smartphone = dk.cachet.carp.common.application.devices.Smartphone;
import WebTask = dk.cachet.carp.common.application.tasks.WebTask;
import ElapsedTimeTrigger = dk.cachet.carp.common.application.triggers.ElapsedTimeTrigger;
import ManualTrigger = dk.cachet.carp.common.application.triggers.ManualTrigger;
import ScheduledTrigger = dk.cachet.carp.common.application.triggers.ScheduledTrigger;
import TaskControl = dk.cachet.carp.common.application.triggers.TaskControl;
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes;
import SelectOne = dk.cachet.carp.common.application.data.input.elements.SelectOne;
import Text = dk.cachet.carp.common.application.data.input.elements.Text;
import AccountIdentity = dk.cachet.carp.common.application.users.AccountIdentity;
import EmailAccountIdentity = dk.cachet.carp.common.application.users.EmailAccountIdentity;
import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute;
import ExpectedParticipantData = dk.cachet.carp.common.application.users.ExpectedParticipantData
import AssignedTo = dk.cachet.carp.common.application.users.AssignedTo
import Roles = dk.cachet.carp.common.application.users.AssignedTo.Roles
import Username = dk.cachet.carp.common.application.users.Username;
import UsernameAccountIdentity = dk.cachet.carp.common.application.users.UsernameAccountIdentity;
import emailAccountIdentityFromString = dk.cachet.carp.common.application.users.EmailAccountIdentity_init_61zpoe$;
import ApiVersion = dk.cachet.carp.common.application.services.ApiVersion
import createDefaultJSON = dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$

import { dk as ddk } from 'carp.core-kotlin-carp.deployments.core'
import DeploymentServiceRequest = ddk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const username = new Username( "Test" )
        const smartphone = new Smartphone( "Role", toSet( [] ) )

        const instances = [
            new EmailAddress( "test@test.com" ),
            EmailAddress.Companion,
            new NamespacedId( "namespace", "type" ),
            NamespacedId.Companion,
            RecurrenceRule.Companion.fromString_61zpoe$( "RRULE:FREQ=WEEKLY;COUNT=10" ),
            RecurrenceRule.Companion,
            TimeOfDay.Companion,
            UUID.Companion.randomUUID(),
            UUID.Companion,
            [ "InputElement", new Text( "How are you feeling?" ) ],
            new SelectOne( "Sex", toSet( [ "Male", "Female" ] ) ),
            SelectOne.Companion,
            new Text( "How are you feeling?" ),
            Text.Companion,
            [ "DeviceConfiguration", smartphone ],
            [ "PrimaryDeviceConfiguration", smartphone ],
            [ "DeviceRegistration", new DefaultDeviceRegistration() ],
            DeviceRegistration.Companion,
            [ "TaskConfiguration", new WebTask( "name", undefined, "", "url.com" ) ],
            new WebTask( "name", undefined, "", "url.com" ),
            WebTask.Companion,
            [ "TriggerConfiguration", new ElapsedTimeTrigger( "device", Duration.Companion.INFINITE ) ],
            new ElapsedTimeTrigger( "device", Duration.Companion.INFINITE ),
            new ManualTrigger( "device", "manual", "" ),
            new ScheduledTrigger(
                "device",
                new TimeOfDay( 10, 10, 10 ),
                RecurrenceRule.Companion.fromString_61zpoe$( "RRULE:FREQ=WEEKLY;COUNT=10" )
            ),
            new TaskControl( 1, "name", "destination", 1 ),
            AccountIdentity.Factory,
            new EmailAccountIdentity( new EmailAddress( "test@test.com" ) ),
            EmailAccountIdentity.Companion,
            username,
            Username.Companion,
            new UsernameAccountIdentity( username ),
            UsernameAccountIdentity.Companion,
            [ "ParticipantAttribute", new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ) ],
            ParticipantAttribute.Companion,
            new ExpectedParticipantData( new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ) ),
            ExpectedParticipantData.Companion,
            AssignedTo.Companion,
            AssignedTo.Anyone,
            new ApiVersion( 1, 0 ),
            [ "ApplicationServiceRequest", new DeploymentServiceRequest.GetStudyDeploymentStatus( UUID.Companion.randomUUID() ) ]
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.common', instances )
        await moduleVerifier.verify()
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

    describe( "ExpectedParticipantData", () => {
        it( "can serialize polymorphic InputBy", () => {
            const expectedData = new ExpectedParticipantData(
                new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ),
                new Roles( toSet( [ "Roles are added" ] ) )
            )

            const json: Json = createDefaultJSON()
            const serializer = ExpectedParticipantData.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, expectedData )
            expect( serialized ).has.string( "Roles are added" )
        } )
    } )
} )
