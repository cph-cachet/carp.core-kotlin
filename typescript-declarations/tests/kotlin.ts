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
        it( "toArray succeeds", () => {
            const list = new ArrayList( [ 1, 2, 3 ] )
            const array = list.toArray()

            expect( array.length ).equals( 3 )
        } )

        it( "size returns length of array", () => {
            const list = new ArrayList( [ 1, 2, 3 ] )

            expect( list.size ).equals( 3 )
        } )
    } )


    describe( "HashSet", () => {
        it( "toArray succeeds", () => {
            const answers = toSet( [ "42" ] )
            const answersArray = answers.toArray()

            expect( answersArray.length ).equals( 1 )
            expect( answersArray[ 0 ] ).equals( "42" )
        } )

        it( "contains succeeds", () => {
            const answers = toSet( [ "42" ] )

            expect( answers.contains_11rb$( "42" ) ).is.true
            expect( answers.contains_11rb$( "nope" ) ).is.false
        } )

        it( "can convert array to set", () => {
            const elements = [ "One", "Two", "Three" ]
            const set = toSet( elements )
            expect( set ).is.not.undefined
        } )
    } )
} )
