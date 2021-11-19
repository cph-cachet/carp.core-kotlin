package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.test.findConcreteTypes
import kotlin.reflect.KClass


val concreteDataTypes: List<KClass<out Data>> = findConcreteTypes()
val concreteInputElementTypes: List<KClass<out InputElement<*>>> = findConcreteTypes()
val concreteDeviceDescriptorTypes: List<KClass<out AnyDeviceDescriptor>> = findConcreteTypes()
val concreteDeviceRegistrationTypes: List<KClass<out DeviceRegistration>> = findConcreteTypes()
val concreteSamplingConfigurationTypes: List<KClass<out SamplingConfiguration>> = findConcreteTypes()
val concreteTaskDescriptorTypes: List<KClass<out TaskDescriptor>> = findConcreteTypes()
val concreteTriggerTypes: List<KClass<out Trigger<*>>> = findConcreteTypes()
val concreteAccountIdentityTypes: List<KClass<out AccountIdentity>> = findConcreteTypes()
