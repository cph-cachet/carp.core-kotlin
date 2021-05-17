package dk.cachet.carp.common.application.services


/**
 * Exposes interactions with internal domain objects which may raise [TIntegrationEvent]s.
 */
interface ApplicationService<
    Self : ApplicationService<Self, TIntegrationEvent>,
    in TIntegrationEvent : IntegrationEvent<Self>
>
