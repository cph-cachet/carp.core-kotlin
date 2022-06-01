import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlinx } from '../src/kotlinx-datetime'
import { dk } from '../src/carp-common'
import createDefaultJSON = dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import Clock = kotlinx.datetime.Clock
import Instant = kotlinx.datetime.Instant
import * as kotlinDateTime from 'Kotlin-DateTime-library-kotlinx-datetime-js-ir'


describe( "kotlinx-datetime", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            kotlinDateTime.$crossModule$.System_getInstance(),
            kotlinDateTime.$crossModule$.System_getInstance().now_0_k$()
        ]

        const moduleVerifier = new VerifyModule(
            'Kotlin-DateTime-library-kotlinx-datetime-js-ir',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "System", () => {
        it( "now succeeds", () => {
            const now: Instant = Clock.System.now()
            expect( now ).not.undefined
        } )
    } )

    describe( "Instant", () => {
        it( "can serialize", () => {
            const now: Instant = Clock.System.now()
            const json = createDefaultJSON( null )
            const serializer = Instant.serializer()
            const serialized = json.encodeToString_onvojc_k$( serializer, now )
    
            expect( serialized ).is.not.undefined
        } )
    } )
} )
