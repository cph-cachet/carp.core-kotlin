import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import ArrayList = kotlin.collections.ArrayList
import HashSet = kotlin.collections.HashSet
import toSet = kotlin.collections.toSet_us0mfu$
import { dk as dkc } from 'carp.core-kotlin-carp.common'
import UUID = dkc.cachet.carp.common.UUID
import { dk } from 'carp.core-kotlin-carp.deployment.core'
import Participation = dk.cachet.carp.deployment.domain.users.Participation
import StudyInvitation = dk.cachet.carp.deployment.domain.users.StudyInvitation
import DeviceDeploymentStatus = dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import StudyDeploymentStatus = dk.cachet.carp.deployment.domain.StudyDeploymentStatus


describe( "carp.deployment.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new Participation( UUID.Companion.randomUUID() ),
            Participation.Companion,
            StudyInvitation.Companion.empty(),
            StudyInvitation.Companion,
            DeviceDeploymentStatus.Companion,
            new DeviceDeploymentStatus.Unregistered( null, true, toSet( [] ), toSet( [] ) ),
            new DeviceDeploymentStatus.Registered( null, true, toSet( [] ), toSet( [] ) ),
            new DeviceDeploymentStatus.Deployed( null ),
            new DeviceDeploymentStatus.NeedsRedeployment( null, toSet( [] ), toSet( [] ) ),
            [ "NotDeployed", new DeviceDeploymentStatus.Unregistered( null, true, toSet( [] ), toSet( [] ) ) ],
            StudyDeploymentStatus.Companion,
            new StudyDeploymentStatus.Invited( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            new StudyDeploymentStatus.DeployingDevices( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            new StudyDeploymentStatus.DeploymentReady( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            new StudyDeploymentStatus.Stopped( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null )
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.deployment.core', instances )
        await moduleVerifier.verify()
    } )
} )
