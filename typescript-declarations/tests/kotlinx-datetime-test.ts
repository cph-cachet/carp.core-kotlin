import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlinx } from '../src/kotlinx-datetime'
import Clock = kotlinx.datetime.Clock


describe( "kotlinx-datetime", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            Clock.System,
            Clock.System.now()
        ]

        const moduleVerifier = new VerifyModule(
            'Kotlin-DateTime-library-kotlinx-datetime-js-ir',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Clock", () => {
        it( "now succeeds", () => {
            const now = Clock.System.now()
            expect( now ).not.undefined
        } )
    } )

    describe( "Instant", () => {
        it( "toEpochMilliseconds succeeds", () => {
            const now = Clock.System.now()

            expect( now.toEpochMilliseconds() ).not.undefined
        } )
    } )
} )