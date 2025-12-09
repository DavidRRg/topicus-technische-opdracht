package nl.topicuszorg.viplivelab.casus.appointment.infrastructure

import nl.topicuszorg.viplivelab.casus.appointment.domain.model.Appointment
import nl.topicuszorg.viplivelab.casus.appointment.domain.port.AppointmentRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.Collections

/**
 * Simple in-memory implementation of [AppointmentRepository].
 */
@Repository
class InMemoryAppointmentRepository : AppointmentRepository {

    /**
     * Internal storage of all appointments, sorted ascending by [Appointment.start].
     */
    private val appointments: MutableList<Appointment> =
        Collections.synchronizedList(mutableListOf())


    /**
     * Inserts the appointment into the list while keeping it sorted by start time.
     */
    override fun save(appointment: Appointment) {
        if (appointments.isEmpty()) {
            appointments.add(appointment)
            return
        }

        // Find the first appointment that starts strictly after the new one.
        val index = appointments.indexOfFirst { it.start > appointment.start }

        if (index == -1) {
            // All existing appointments start before or at the same time,
            // so we append this one at the end.
            appointments.add(appointment)
        } else {
            // Insert at the correct position to keep the list sorted.
            appointments.add(index, appointment)
        }
    }

    /**
     * Returns all appointments that overlap with the given ([start], [end]) range.
     *
     * Uses the classic half open interval rule:
     * two ranges (aStart, aEnd) and (bStart, bEnd) overlap if:
     *
     *   aStart < bEnd && aEnd > bStart
     *
     * Because the list is sorted by start time, we can stop scanning as soon as
     * an appointment starts at or after [end]. Any later appointment will then
     * also start at or after [end] and can never overlap.
     */
    override fun findOverlapping(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): List<Appointment> {
        val result = mutableListOf<Appointment>()

        for (appointment in appointments) {
            val apptStart = appointment.start
            val apptEnd = appointment.end

            // If the appointment starts on or after the end of the range,
            // we know that no later appointment can overlap anymore.
            if (!apptStart.isBefore(end)) {
                break
            }

            // Classic overlap check: [apptStart, apptEnd) vs [start, end)
            if (apptStart < end && apptEnd > start) {
                result.add(appointment)
            }
        }
        return result
    }

    /**
     * Returns all appointments whose start is between [start] and [end].
     *
     * If [end] is null, all appointments starting at or after [start] are returned.
     *
     * Because the list is sorted by start time, we can break early once we pass [end].
     */
    override fun findBetween(
        start: ZonedDateTime,
        end: ZonedDateTime?
    ): List<Appointment> {
        val result = mutableListOf<Appointment>()

        for (appointment in appointments) {
            val apptStart = appointment.start

            if (end != null && !apptStart.isBefore(end)) {
                // Once we pass the end of the window, we can stop.
                break
            }

            if (!apptStart.isBefore(start)) {
                result.add(appointment)
            }
        }

        return result
    }
}
