import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import ArrayList = kotlin.collections.ArrayList
import HashSet = kotlin.collections.HashSet
import toSet = kotlin.collections.toSet_us0mfu$
import { dk as dkc } from 'carp.common'
import UUID = dkc.cachet.carp.common.UUID
import { dk } from 'carp.deployment.core'
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
            new DeviceDeploymentStatus.Unregistered( null, true, toSet( [] ) ),
            new DeviceDeploymentStatus.Registered( null, true, toSet( [] ) ),
            new DeviceDeploymentStatus.Deployed( null ),
            new DeviceDeploymentStatus.NeedsRedeployment( null, toSet( [] ) ),
            [ "NotDeployed", new DeviceDeploymentStatus.Unregistered( null, true, toSet( [] ) ) ],
            StudyDeploymentStatus.Companion,
            new StudyDeploymentStatus.Invited( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ) ),
            new StudyDeploymentStatus.DeployingDevices( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ) ),
            new StudyDeploymentStatus.DeploymentReady( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ) ),
            new StudyDeploymentStatus.Stopped( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ) )
        ]

        const moduleVerifier = new VerifyModule( 'carp.deployment.core', instances )
        await moduleVerifier.verify()
    } )
} )
