import VerifyModule from './VerifyModule'


describe( "kotlinx-serialization", () => {
    it( "verify module declarations", async () => {
        const instances = new Map<string, any>()

        const moduleVerifier = new VerifyModule(
            'kotlinx-serialization-kotlinx-serialization-runtime',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
