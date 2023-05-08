package dk.cachet.carp.common.application.services


/**
 * Exposes interactions with internal domain objects which may raise [TIntegrationEvent]s.
 */
// @JsExport can't be applied due to a bug: https://youtrack.jetbrains.com/issue/KT-57356/
interface ApplicationService<
    Self : ApplicationService<Self, TIntegrationEvent>,
    in TIntegrationEvent : IntegrationEvent<Self>
>
