package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.test.findConcreteTypes
import kotlin.reflect.KClass


val concreteDataTypes: List<KClass<out Data>> = findConcreteTypes()
val concreteInputElementTypes: List<KClass<out InputElement<*>>> = findConcreteTypes()
val concreteDeviceConfigurationTypes: List<KClass<out AnyDeviceConfiguration>> = findConcreteTypes()
val concreteDeviceRegistrationTypes: List<KClass<out DeviceRegistration>> = findConcreteTypes()
val concreteSamplingConfigurationTypes: List<KClass<out SamplingConfiguration>> = findConcreteTypes()
val concreteTaskConfigurationTypes: List<KClass<out TaskConfiguration<*>>> = findConcreteTypes()
val concreteTriggerConfigurationTypes: List<KClass<out TriggerConfiguration<*>>> = findConcreteTypes()
val concreteAccountIdentityTypes: List<KClass<out AccountIdentity>> = findConcreteTypes()
