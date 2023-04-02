import VerifyModule from './VerifyModule'


describe( "carp-deployments-core", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = []

        const moduleVerifier = new VerifyModule(
            'carp-deployments-core-generated',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
