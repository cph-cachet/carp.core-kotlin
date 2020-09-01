import VerifyModule from './VerifyModule'


describe( "kotlinx-serialization", () => {
    it( "verify module declarations", async () => {
        const instances = new Array<any>()

        const moduleVerifier = new VerifyModule(
            'kotlinx-serialization-kotlinx-serialization-core-jsLegacy',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
