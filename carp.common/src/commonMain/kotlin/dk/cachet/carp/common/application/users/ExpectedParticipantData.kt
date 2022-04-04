package dk.cachet.carp.common.application.users

import dk.cachet.carp.common.application.data.input.InputDataType
import kotlinx.serialization.Serializable


/**
 * Describes a participant [attribute] that pertains to all or specified participants in a study.
 */
@Serializable
data class ExpectedParticipantData(
    val attribute: ParticipantAttribute,
    /**
     * Determines whether the attribute can be set by [AssignedTo.All] participants in the study (one field for all),
     * or an individual attribute can be set by each of the specified [AssignedTo.Roles] (one field per role).
     */
    val assignedTo: AssignedTo = AssignedTo.All
)
{
    val inputDataType: InputDataType
        get() = attribute.inputDataType
}


/**
 * Determines whether the set contains any of the following conflicts:
 * - contains differing participant attributes with the same input data type
 * - contains multiple attributes of the same input data type which are assigned to the same role
 *
 * @throws IllegalArgumentException if [exceptionOnConflict] is set to true and the set contains a conflict.
 */
fun Set<ExpectedParticipantData>.hasNoConflicts( exceptionOnConflict: Boolean = false ): Boolean
{
    val expectedDataByInputType = this.groupBy { it.inputDataType }

    // Check for differing `ParticipantAttribute`s with the same input data type.
    val noConflictingAttributes = expectedDataByInputType
        .all {
            val firstAttribute: ParticipantAttribute? = it.value.firstOrNull()?.attribute
            it.value.all { expectedData -> expectedData.attribute == firstAttribute }
        }
    if ( exceptionOnConflict )
    {
        require( noConflictingAttributes )
            { "Expected data contains differing participant attributes with the same input data type." }
    }

    // Check for multiple attributes of the same input data type which are assigned to the same role.
    val noMultipleInputType = expectedDataByInputType
        .all { (_, expectedData) ->
            if ( AssignedTo.All in expectedData.map { it.assignedTo } )
            {
                // Any additional expected data would specify roles and thus conflict with the `All` configuration.
                expectedData.size == 1
            }
            else
            {
                // Duplicates indicate the same role is configured to input the same input data type multiple times.
                val canBeInputBy = expectedData.flatMap { (it.assignedTo as AssignedTo.Roles).roleNames }
                canBeInputBy.size == canBeInputBy.distinct().size
            }
        }
    if ( exceptionOnConflict )
    {
        require( noMultipleInputType )
            { "Expected data contains attributes of the same input data type which are assigned to the same role." }
    }

    return noConflictingAttributes && noMultipleInputType
}
