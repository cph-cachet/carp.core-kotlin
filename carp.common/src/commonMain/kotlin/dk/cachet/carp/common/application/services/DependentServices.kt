package dk.cachet.carp.common.application.services

import kotlin.reflect.KClass


/**
 * Lists services this service is dependent on since it subscribes to events it emits.
 */
@Target( AnnotationTarget.CLASS )
@Retention( AnnotationRetention.RUNTIME )
annotation class DependentServices( vararg val service: KClass<out ApplicationService<*, *>> )
