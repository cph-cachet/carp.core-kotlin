import VerifyModule from './VerifyModule'

import { expect } from 'chai'


describe( "kotlinx-serialization", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
        ]

        const moduleVerifier = new VerifyModule(
            'kotlinx-serialization-kotlinx-serialization-json-js-ir',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
