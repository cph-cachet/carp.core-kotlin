import VerifyModule from './VerifyModule'


describe( "kotlinx-serialization", () => {
    it( "verify core module declarations", async () => {
        const instances = new Array<any>()

        const moduleVerifier = new VerifyModule(
            'kotlinx-serialization-kotlinx-serialization-core-jsLegacy',
            instances
        )
        await moduleVerifier.verify()
    } )

    it( "verify json module declarations", async () => {
        const instances = new Array<any>()

        const moduleVerifier = new VerifyModule(
            'kotlinx-serialization-kotlinx-serialization-json-jsLegacy',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
