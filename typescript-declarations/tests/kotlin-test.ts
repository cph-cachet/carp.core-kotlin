import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlin } from '../src/kotlin'
import toLong = kotlin.toLong
import listOf = kotlin.collections.listOf
import setOf = kotlin.collections.setOf
import contains = kotlin.collections.contains


describe( "kotlin", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            toLong( 42 ),
            [ "Collection", setOf( [ "test" ] )],
            [ "List", listOf( [ "test" ] ) ],
            [ "Set", setOf( [ "test" ] ) ]
        ]

        const moduleVerifier = new VerifyModule(
            'kotlin-kotlin-stdlib-js-ir',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Long", () => {
        it( "toLong and back toInt equals", () => {
            const answer = toLong( 42 )
            const answerAsNumber: Number = answer.toInt_0_k$()

            expect( answerAsNumber ).equals( 42 )
        } )
    } )

    describe( "List", () => {
        it( "listOf and conversion back to array succeeds", () => {
            const numbers = [ 1, 2, 3 ]
            const numbersList = listOf( numbers )
            const numbersArray = numbersList.toArray()

            expect( numbersArray.length ).equals( numbers.length )
        } )

        it( "contains succeeds", () => {
            const includesAnswer = listOf( [ 0, 42, 50 ] )

            expect( contains( includesAnswer, 42 ) ).is.true
            expect( contains( includesAnswer, 100 ) ).is.false
        } )

        it( "size succeeds", () => {
            const three = listOf( [ 1, 2, 3 ] )
            const size = three._get_size__0_k$()

            expect( size ).equals( 3 )
        } )
    } )

    describe( "Set", () => {
        it( "setOf and conversion back to array succeeds", () => {
            const answers = [ 42, 0 ]
            const answersSet = setOf( answers )
            const answersArray = answersSet.toArray()

            expect( answersArray.length ).equals( answers.length )
        } )

        it( "contains succeeds", () => {
            const includesAnswer = setOf( [ 0, 42, 50 ] )

            expect( contains( includesAnswer, 42 ) ).is.true
            expect( contains( includesAnswer, 100 ) ).is.false
        } )

        it( "size succeeds", () => {
            const three = setOf( [ 1, 2, 3 ] )
            const size = three._get_size__0_k$()

            expect( size ).equals( 3 )
        } )
    } )
} )
