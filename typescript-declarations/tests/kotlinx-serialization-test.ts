import VerifyModule from './VerifyModule'

import { expect } from 'chai'
import { kotlinx } from '../src/kotlinx-serialization'
import Json = kotlinx.serialization.Json


describe( "kotlinx-datetime", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            [ "JsonImpl", Json.Default ]
        ]

        const moduleVerifier = new VerifyModule(
            'kotlinx-serialization-kotlinx-serialization-json-js-ir',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
