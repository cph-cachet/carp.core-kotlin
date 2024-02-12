import VerifyModule from './VerifyModule.js'

import { expect } from 'chai'
import kotlinx from '@cachet/carp-kotlinx-datetime'
import Clock = kotlinx.datetime.Clock


describe( "kotlinx-datetime", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            Clock.System,
            Clock.System.now()
        ]

        const moduleVerifier = new VerifyModule(
            '@cachet/Kotlin-DateTime-library-kotlinx-datetime-js-ir',
            './carp-kotlinx-datetime/Kotlin-DateTime-library-kotlinx-datetime-js-ir.d.ts',
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
