import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { Long } from 'kotlin'


describe( "kotlin", () => {
    it( "verify module declarations", async () => {
        const instances = new Map<string, any>()

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
} )
