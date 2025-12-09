package nl.topicuszorg.viplivelab.casus.appointment.domain.service

import nl.topicuszorg.viplivelab.casus.appointment.domain.model.Appointment
import java.time.Duration
import java.time.ZonedDateTime

/**
 * Core business service for managing appointments and availability.
 *
 * Responsibilities:
 * - validate appointment time ranges
 * - ensure no overlapping appointments
 * - create new appointments
 * - compute the next free slot for a requested duration
 */
interface AppointmentService {

    /**
     * Creates a new appointment if it does not violate any business rules.
     *
     * Expected behaviour in the implementation:
     * - validate that start is strictly before end
     * - check for overlapping appointments using the repository
     * - generate a new UUID for the appointment id
     */
    fun createAppointment(
        start: ZonedDateTime,
        end: ZonedDateTime,
        description: String? = null
    ): Appointment

    /**
     * Finds the next free slot of [duration], starting at [from].
     *
     * @param searchUntil Optional upper bound for the search window.
     *                    If no slot can fit before this moment,
     *                    NoFreeSlotAvailableException should be thrown.
     */
    fun findNextFreeSlot(
        from: ZonedDateTime,
        duration: Duration,
        searchUntil: ZonedDateTime? = null
    ): ZonedDateTime
}
