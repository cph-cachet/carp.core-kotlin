package dk.cachet.carp.common.application.services

import kotlin.reflect.KClass


/**
 * Lists services this service is dependent on since it subscribes to events it emits.
 */
@Target( AnnotationTarget.CLASS )
// HACK: For clarity, showing this is a runtime annotation (which is the default) is desirable.
//  But, doing so triggers a compiler warning: https://youtrack.jetbrains.com/issue/KT-41082
// @Retention( AnnotationRetention.RUNTIME )
annotation class DependentServices( vararg val service: KClass<out ApplicationService<*, *>> )
