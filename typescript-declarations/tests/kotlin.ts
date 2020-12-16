import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { Long } from 'kotlin'
import { kotlin } from 'kotlin'
import Pair = kotlin.Pair
import ArrayList = kotlin.collections.ArrayList
import HashSet = kotlin.collections.HashSet
import toSet = kotlin.collections.toSet_us0mfu$


describe( "kotlin", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new Pair( "key", "value" ),
            new HashSet(),
            new ArrayList( [ "One", "Two", "Three" ] )
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


    describe( "ArrayList", () => {
        it( "access internal array", () => {
            const array = [ 1, 2 ]
            const list = new ArrayList( array )

            expect( list.array_hd7ov6$_0 ).equals( array )
        } )

        it( "size returns length of array", () => {
            const list = new ArrayList( [ 1, 2, 3 ] )

            expect( list.size ).equals( 3 )
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
