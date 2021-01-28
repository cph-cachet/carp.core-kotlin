package dk.cachet.carp.common.ddd


/**
 * Exposes interactions with internal domain objects which may raise [TIntegrationEvent]s.
 */
interface ApplicationService<
    Self : ApplicationService<Self, TIntegrationEvent>,
    in TIntegrationEvent : IntegrationEvent<Self>
>
