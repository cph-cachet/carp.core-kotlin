import VerifyModule from './VerifyModule'


describe( "carp.deployments.core", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = []

        const moduleVerifier = new VerifyModule(
            'carp.core-kotlin-carp.deployments.core',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
