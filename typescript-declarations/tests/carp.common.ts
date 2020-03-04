import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { Long } from 'kotlin'
import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-runtime'
import Json = kotlinx.serialization.json.Json
import { dk } from "carp.common"
import DateTime = dk.cachet.carp.common.DateTime
import EmailAddress = dk.cachet.carp.common.EmailAddress
import createDefaultJSON = dk.cachet.carp.common.serialization.createDefaultJSON_stpyu4$


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const instances = new Map<string, any>( [
            [ "DateTime", DateTime.Companion.now() ],
            [ "EmailAddress", new EmailAddress( "test@test.com" ) ]
        ] )

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
} )
