import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { Long } from 'kotlin'
import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-runtime'
import Json = kotlinx.serialization.json.Json
import { dk } from "carp.common"
import DateTime = dk.cachet.carp.common.DateTime
import EmailAddress = dk.cachet.carp.common.EmailAddress
import TimeSpan = dk.cachet.carp.common.TimeSpan
import Trilean = dk.cachet.carp.common.Trilean
import toTrilean = dk.cachet.carp.common.toTrilean_1v8dcc$
import UUID = dk.cachet.carp.common.UUID
import AccountIdentity = dk.cachet.carp.common.users.AccountIdentity
import EmailAccountIdentity = dk.cachet.carp.common.users.EmailAccountIdentity
import emailAccountIdentityFromString = dk.cachet.carp.common.users.EmailAccountIdentity_init_61zpoe$
import UsernameAccountIdentity = dk.cachet.carp.common.users.UsernameAccountIdentity
import createDefaultJSON = dk.cachet.carp.common.serialization.createDefaultJSON_stpyu4$


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const instances = [
            DateTime.Companion.now(),
            DateTime.Companion,
            new EmailAddress( "test@test.com" ),
            EmailAddress.Companion,
            TimeSpan.Companion.INFINITE,
            TimeSpan.Companion,
            UUID.Companion.randomUUID(),
            UUID.Companion,
            AccountIdentity.Factory,
            new EmailAccountIdentity( new EmailAddress( "test@test.com" ) ),
            EmailAccountIdentity.Companion,
            new UsernameAccountIdentity( "Test" ),
            UsernameAccountIdentity.Companion
        ]

        const moduleVerifier = new VerifyModule( 'carp.common', instances )
        await moduleVerifier.verify()
    } )


    describe( "DateTime", () => {
        it( "serializes as string", () => {
            const dateTime = new DateTime( Long.fromNumber( 42 ) )
            
            const json: Json = createDefaultJSON()
            const serializer = DateTime.Companion.serializer()
            const serialized = json.stringify_tf03ej$( serializer, dateTime )
    
            expect( serialized ).equals( "42" )
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
} )
