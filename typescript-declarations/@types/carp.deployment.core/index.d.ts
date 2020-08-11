declare module 'carp.deployment.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashSet = kotlin.collections.HashSet
    import { dk as cdk } from 'carp.common'
    import UUID = cdk.cachet.carp.common.UUID
    import DateTime = cdk.cachet.carp.common.DateTime


    namespace dk.cachet.carp.deployment.domain
    {
        class DeviceDeploymentStatus
        {
            static get Companion(): DeviceDeploymentStatus$Companion
        }
        interface DeviceDeploymentStatus$Companion { serializer(): any }

        namespace DeviceDeploymentStatus
        {
            interface NotDeployed
            {
                readonly requiresDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>

            }
            class Unregistered implements NotDeployed
            {
                constructor(
                    device: any,
                    requiresDeployment: Boolean,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<String>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<String> )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>
            }
            class Registered implements NotDeployed
            {
                constructor(
                    device: any,
                    requiresDeployment: Boolean,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<String>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<String> )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>
            }
            class Deployed
            {
                constructor( device: any )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
            }
            class NeedsRedeployment implements NotDeployed
            {
                constructor(
                    device: any,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<String>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<String> )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>
            }
        }


        class StudyDeploymentStatus
        {
            static get Companion(): StudyDeploymentStatus$Companion
        }
        interface StudyDeploymentStatus$Companion { serializer(): any }

        namespace StudyDeploymentStatus
        {
            class Invited
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
            class DeployingDevices
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null  )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
            class DeploymentReady
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null  )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
            class Stopped
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null  )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
        }
    }


    namespace dk.cachet.carp.deployment.domain.users
    {
        class Participation
        {
            constructor( studyDeploymentId: UUID, id?: UUID )

            static get Companion(): Participation$Companion

            readonly studyDeploymentId: UUID
            readonly id: UUID
        }
        interface Participation$Companion { serializer(): any }


        class StudyInvitation
        {
            constructor( name: string, description: string, applicationData?: string )

            static get Companion(): StudyInvitation$Companion

            readonly name: string
            readonly description: string
            readonly applicationData: string
        }
        interface StudyInvitation$Companion
        {
            serializer(): any;
            empty(): StudyInvitation;
        }
    }
}