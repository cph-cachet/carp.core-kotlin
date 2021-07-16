import VerifyModule from './VerifyModule'

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json

import { kotlinx as kxd } from 'Kotlin-DateTime-library-kotlinx-datetime-js-legacy'
import Clock = kxd.datetime.Clock
import Instant = kxd.datetime.Instant

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import createDefaultJSON = cdk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$
import { expect } from 'chai'


describe( "kotlinx-datetime", () => {
    it( "verify module declarations", async () => {
        const instances = [
            Clock.System,
            [ "Instant$Companion", Instant.Companion ]
        ]

        const moduleVerifier = new VerifyModule(
            'Kotlin-DateTime-library-kotlinx-datetime-js-legacy',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Instant", () => {
        it( "can serialize", () => {
            const now: Instant = Clock.System.now()
            const json: Json = createDefaultJSON()
            const serializer = Instant.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, now )

            expect(serialized).is.not.undefined
        } )
    } )
} )
