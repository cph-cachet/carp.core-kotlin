import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlinx } from '@cachet/carp-kotlinx-serialization'
import Json = kotlinx.serialization.json.Json


describe( "kotlinx-serialization", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            [ "JsonImpl", Json.Default ]
        ]

        const moduleVerifier = new VerifyModule(
            '@cachet/kotlinx-serialization-kotlinx-serialization-json-js-ir',
            './carp-kotlinx-serialization/kotlinx-serialization-kotlinx-serialization-json-js-ir.d.ts',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
