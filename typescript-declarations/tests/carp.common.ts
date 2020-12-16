import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import { Long } from 'kotlin'
import toSet = kotlin.collections.toSet_us0mfu$
import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
import Json = kotlinx.serialization.json.Json
import { dk } from 'carp.core-kotlin-carp.common'
import DateTime = dk.cachet.carp.common.DateTime
import EmailAddress = dk.cachet.carp.common.EmailAddress
import NamespacedId = dk.cachet.carp.common.NamespacedId
import TimeSpan = dk.cachet.carp.common.TimeSpan
import Trilean = dk.cachet.carp.common.Trilean
import toTrilean = dk.cachet.carp.common.toTrilean_1v8dcc$
import UUID = dk.cachet.carp.common.UUID
import AccountIdentity = dk.cachet.carp.common.users.AccountIdentity
import EmailAccountIdentity = dk.cachet.carp.common.users.EmailAccountIdentity
import emailAccountIdentityFromString = dk.cachet.carp.common.users.EmailAccountIdentity_init_61zpoe$
import UsernameAccountIdentity = dk.cachet.carp.common.users.UsernameAccountIdentity
import createDefaultJSON = dk.cachet.carp.common.serialization.createDefaultJSON_18xi4u$
import ParticipantAttribute = dk.cachet.carp.common.users.ParticipantAttribute
import SelectOne = dk.cachet.carp.common.data.input.element.SelectOne
import Text = dk.cachet.carp.common.data.input.element.Text
import CarpInputDataTypes = dk.cachet.carp.common.data.input.CarpInputDataTypes


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const instances = [
            DateTime.Companion.now(),
            DateTime.Companion,
            new EmailAddress( "test@test.com" ),
            EmailAddress.Companion,
            new NamespacedId( "namespace", "type" ),
            NamespacedId.Companion,
            TimeSpan.Companion.INFINITE,
            TimeSpan.Companion,
            UUID.Companion.randomUUID(),
            UUID.Companion,
            AccountIdentity.Factory,
            new EmailAccountIdentity( new EmailAddress( "test@test.com" ) ),
            EmailAccountIdentity.Companion,
            new UsernameAccountIdentity( "Test" ),
            UsernameAccountIdentity.Companion,
            [ "ParticipantAttribute", new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ) ],
            ParticipantAttribute.Companion,
            [ "InputElement", new Text( "How are you feeling?" ) ],
            SelectOne.Companion,
            new SelectOne( "Sex", toSet( [ "Male", "Female" ] ) ),
            Text.Companion,
            new Text( "How are you feeling?" )
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.common', instances )
        await moduleVerifier.verify()
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
            const inputElement = attribute.getInputElement_zbztje$( CarpInputDataTypes )
            expect( inputElement ).instanceOf( Text )
        } )

        it( "isValidInput works", () => {
            const isNumberValid = attribute.isValidInput_jon1ci$( CarpInputDataTypes, 42 )
            expect( isNumberValid ).is.false

            const isStringValid = attribute.isValidInput_jon1ci$( CarpInputDataTypes, "Steven" )
            expect( isStringValid ).is.true
        } )

        it( "inputToData works", () => {
            const data = attribute.inputToData_jon1ci$( CarpInputDataTypes, "Steven" )
            expect( data ).is.not.undefined
        } )
    } )
} )
