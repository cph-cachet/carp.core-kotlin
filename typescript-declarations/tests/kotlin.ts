import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { Long } from 'kotlin'
import { kotlin } from 'kotlin'
import HashSet = kotlin.collections.HashSet
import toSet = kotlin.collections.toSet_us0mfu$


describe( "kotlin", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new HashSet()
        ]

        const moduleVerifier = new VerifyModule( 'kotlin', instances )
        await moduleVerifier.verify()
    } )


    describe( "Long", () => {
        it( "fromNumber and toNumber equals", () => {
            const answer = Long.fromNumber( 42 )
            const answerAsNumber = answer.toNumber()
    
            expect( answerAsNumber ).equals( 42 )
        } )
    } )


    describe( "Set", () => {
        it ( "can convert array to set", () => {
            const elements = [ "One", "Two", "Three" ]
            const set = toSet( elements )
            expect( set ).is.not.undefined
        } )
    } )
} )
