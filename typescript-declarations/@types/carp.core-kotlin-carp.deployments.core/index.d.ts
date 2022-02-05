declare module 'carp.core-kotlin-carp.deployments.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashMap = kotlin.collections.HashMap
    import HashSet = kotlin.collections.HashSet

    import { kotlinx as kxd } from 'Kotlin-DateTime-library-kotlinx-datetime-js-legacy'
    import Instant = kxd.datetime.Instant

    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import NamespacedId = cdk.cachet.carp.common.application.NamespacedId
    import UUID = cdk.cachet.carp.common.application.UUID
    import DeviceRegistration = cdk.cachet.carp.common.application.devices.DeviceRegistration
    import AccountIdentity = cdk.cachet.carp.common.application.users.AccountIdentity

    import { dk as pdk } from 'carp.core-kotlin-carp.protocols.core'
    import StudyProtocolSnapshot = pdk.cachet.carp.protocols.application.StudyProtocolSnapshot


    namespace dk.cachet.carp.deployments.application
    {
        import ParticipantStatus = dk.cachet.carp.deployments.application.users.ParticipantStatus


        abstract class DeviceDeploymentStatus
        {
            readonly device: any
            readonly canBeDeployed: Boolean
            readonly canObtainDeviceDeployment: Boolean

            static get Companion(): DeviceDeploymentStatus$Companion
        }
        interface DeviceDeploymentStatus$Companion { serializer(): any }

        namespace DeviceDeploymentStatus
        {
            abstract class NotDeployed
            {
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<string>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<string>

            }
            class Unregistered extends NotDeployed
            {
                constructor(
                    device: any,
                    canBeDeployed: Boolean,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<string>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<string> )
            }
            class Registered extends NotDeployed
            {
                constructor(
                    device: any,
                    canBeDeployed: Boolean,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<string>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<string> )
            }
            class Deployed extends DeviceDeploymentStatus 
            {
                constructor( device: any )
            }
            class NeedsRedeployment extends NotDeployed
            {
                constructor(
                    device: any,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<string>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<string> )
            }
        }


        class MasterDeviceDeployment
        {
            constructor(
                deviceDescriptor: any,
                registration: DeviceRegistration,
                connectedDevices?: HashSet<any>,
                connectedDeviceConfigurations?: HashMap<string, DeviceRegistration>,
                tasks?: HashSet<any>,
                triggers?: HashMap<number, any>,
                taskControls?: HashSet<any>,
                applicationData?: string | null )

                static get Companion(): MasterDeviceDeployment$Companion

                readonly deviceDescriptor: any
                readonly registration: DeviceRegistration
                readonly connectedDevices: HashSet<any>
                readonly connectedDeviceRegistrations: HashMap<string, DeviceRegistration>
                readonly tasks: HashSet<any>
                readonly triggers: HashMap<number, any>
                readonly taskControls: HashSet<any>
                readonly applicationData: string | null
        }
        interface MasterDeviceDeployment$Companion { serializer(): any }


        abstract class StudyDeploymentStatus
        {
            readonly createdOn: Instant
            readonly studyDeploymentId: UUID
            readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
            readonly participantsStatus: ArrayList<ParticipantStatus>
            readonly startedOn: Instant | null

            static get Companion(): StudyDeploymentStatus$Companion
        }
        interface StudyDeploymentStatus$Companion { serializer(): any }

        namespace StudyDeploymentStatus
        {
            class Invited extends StudyDeploymentStatus
            {
                constructor( createdOn: Instant, studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, participantsStatus: ArrayList<ParticipantStatus>, startedOn: Instant | null )
            }
            class DeployingDevices extends StudyDeploymentStatus
            {
                constructor( createdOn: Instant, studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, participantsStatus: ArrayList<ParticipantStatus>, startedOn: Instant | null  )
            }
            class Running extends StudyDeploymentStatus
            {
                constructor( createdOn: Instant, studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, participantsStatus: ArrayList<ParticipantStatus>, startedOn: Instant )
            }
            class Stopped extends StudyDeploymentStatus
            {
                constructor( createdOn: Instant, studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, participantsStatus: ArrayList<ParticipantStatus>, startedOn: Instant | null, stoppedOn: Instant  )

                readonly stoppedOn: Instant
            }
        }
    }


    namespace dk.cachet.carp.deployments.application.users
    {
        class ActiveParticipationInvitation
        {
            constructor( participation: Participation, invitation: StudyInvitation, assignedDevices: HashSet<AssignedMasterDevice> )

            static get Companion(): ActiveParticipationInvitation$Companion

            readonly participation: Participation
            readonly invitation: StudyInvitation
            readonly assignedDevices: HashSet<AssignedMasterDevice>
        }
        interface ActiveParticipationInvitation$Companion { serializer(): any }

        class AssignedMasterDevice
        {
            constructor( device: any, registration: DeviceRegistration | null )

            static get Companion(): AssignedMasterDevice$Companion

            readonly device: any
            readonly registration: DeviceRegistration | null
        }
        interface AssignedMasterDevice$Companion { serializer(): any }

        class Participation
        {
            constructor( studyDeploymentId: UUID, participantId?: UUID )

            static get Companion(): Participation$Companion

            readonly studyDeploymentId: UUID
            readonly participantId: UUID
        }
        interface Participation$Companion { serializer(): any }

        class ParticipantData
        {
            constructor( studyDeploymentId: UUID, data: HashMap<NamespacedId, any> )

            static get Companion(): ParticipantData$Companion

            readonly studyDeploymentId: UUID
            readonly data: HashMap<NamespacedId, any>
        }
        interface ParticipantData$Companion { serializer(): any }

        class ParticipantInvitation
        {
            constructor( participantId: UUID, assignedMasterDeviceRoleNames: HashSet<string>, identity: AccountIdentity, invitation: StudyInvitation )

            readonly participantId: UUID
            readonly assignedMasterDeviceRoleNames: HashSet<string>
            readonly identity: AccountIdentity
            readonly invitation: StudyInvitation

            static get Companion(): ParticipantInvitation$Companion
        }
        interface ParticipantInvitation$Companion { serializer(): any }

        class ParticipantStatus
        {
            constructor( participantId: UUID, assignedMasterDeviceRoleNames: HashSet<string> )

            readonly participantId: UUID
            readonly assignedMasterDeviceRoleNames: HashSet<string>

            static get Companion(): ParticipantStatus$Companion
        }
        interface ParticipantStatus$Companion { serializer(): any }

        class StudyInvitation
        {
            constructor( name: string, description?: string | null, applicationData?: string | null )

            static get Companion(): StudyInvitation$Companion

            readonly name: string
            readonly description: string | null
            readonly applicationData: string | null
        }
        interface StudyInvitation$Companion
        {
            serializer(): any;
        }
    }


    namespace dk.cachet.carp.deployments.infrastructure
    {
        import ParticipantInvitation = dk.cachet.carp.deployments.application.users.ParticipantInvitation


        abstract class DeploymentServiceRequest
        {
            static get Serializer(): any
        }

        namespace DeploymentServiceRequest
        {
            class CreateStudyDeployment extends DeploymentServiceRequest
            {
                constructor( protocol: StudyProtocolSnapshot, invitations: ArrayList<ParticipantInvitation>, connectedDevicePreregistrations?: HashMap<string, DeviceRegistration> )
            }
            class GetStudyDeploymentStatus extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID )
            }
            class GetStudyDeploymentStatusList extends DeploymentServiceRequest
            {
                constructor( studyDeploymentIds: HashSet<UUID> )
            }
            class RegisterDevice extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, deviceRoleName: string, registration: DeviceRegistration )
            }
            class UnregisterDevice extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, deviceRoleName: string )
            }
            class GetDeviceDeploymentFor extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, masterDeviceRoleName: string )
            }
            class DeviceDeployed extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, masterDeviceRoleName: string, deviceDeploymentLastUpdatedOn: Instant )
            }
            class Stop extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID )
            }
        }


        abstract class ParticipationServiceRequest
        {
            static get Serializer(): any
        }

        namespace ParticipationServiceRequest
        {
            class GetActiveParticipationInvitations extends ParticipationServiceRequest
            {
                constructor( accountId: UUID )
            }
            class GetParticipantData extends ParticipationServiceRequest
            {
                constructor( studyDeploymentId: UUID )
            }
            class GetParticipantDataList extends ParticipationServiceRequest
            {
                constructor( studyDeploymentIds: HashSet<UUID> )
            }
            class SetParticipantData extends ParticipationServiceRequest
            {
                constructor( studyDeploymentId: UUID, data: HashMap<NamespacedId, any | null> )
            }
        }
    }
}