package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID


interface DeploymentRepository
{
    /**
     * Adds the specified [deployment].
     *
     * @deployment The [Deployment] to add.
     */
    fun add( deployment: Deployment )

    /**
     * Gets the [Deployment] with the specified [id].
     *
     * @id The id of the [Deployment] to search for.
     * @throws IllegalArgumentException when a deployment with [id] does not exist.
     */
    fun getBy( id: UUID ): Deployment
}