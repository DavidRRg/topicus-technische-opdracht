package nl.topicuszorg.viplivelab.casus.appointment.domain.port

import nl.topicuszorg.viplivelab.casus.appointment.domain.model.Appointment
import java.time.ZonedDateTime

/**
 * Repository abstraction for storing and querying appointments.
 */
interface AppointmentRepository {

    /**
     * Persist a new appointment.
     */
    fun save(appointment: Appointment)

    /**
     * Returns all appointments that overlap with the given time range.
     *
     * Used by the service to decide if a new appointment can be scheduled
     * without violating the "no overlap" rule.
     */
    fun findOverlapping(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): List<Appointment>

    /**
     * Returns all appointments that start inside the given window,
     * or affect it in a meaningful way, depending on implementation.
     *
     * Used to search for the next free slot starting at [start].
     * If [end] is null, the search range is open ended.
     */
    fun findBetween(
        start: ZonedDateTime,
        end: ZonedDateTime? = null
    ): List<Appointment>
}
