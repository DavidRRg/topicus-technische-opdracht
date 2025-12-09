package nl.topicuszorg.viplivelab.casus.appointment.infrastructure.service

import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.AppointmentConflictException
import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.InvalidAppointmentTimeException
import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.NoFreeSlotAvailableException
import nl.topicuszorg.viplivelab.casus.appointment.domain.model.Appointment
import nl.topicuszorg.viplivelab.casus.appointment.domain.port.AppointmentRepository
import nl.topicuszorg.viplivelab.casus.appointment.domain.service.AppointmentService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Default implementation of [AppointmentService].
 *
 * Responsibilities:
 * - validate appointment time ranges (start < end)
 * - prevent overlapping appointments
 * - create and persist appointments
 * - compute the next free slot for a requested duration
 */
@Service
class AppointmentServiceImpl(
    private val repository: AppointmentRepository
) : AppointmentService {

    override fun createAppointment(
        start: ZonedDateTime,
        end: ZonedDateTime,
        description: String?
    ): Appointment {
        // Basic validation: start must be strictly before end
        if (!start.isBefore(end)) {
            throw InvalidAppointmentTimeException("Appointment start must be before end")
        }

        // Check overlap with existing appointments
        val overlapping = repository.findOverlapping(start, end)
        if (overlapping.isNotEmpty()) {
            throw AppointmentConflictException(
                "Appointment overlaps with ${overlapping.size} existing appointment(s)"
            )
        }

        // If all checks pass, create and persist appointment
        val appointment = Appointment(
            id = UUID.randomUUID(),
            start = start,
            end = end,
            description = description
        )

        repository.save(appointment)
        return appointment
    }

    override fun findNextFreeSlot(
        from: ZonedDateTime,
        duration: Duration,
        searchUntil: ZonedDateTime?
    ): ZonedDateTime {

        if (duration.isZero || duration.isNegative) {
            throw IllegalArgumentException("Duration must be positive")
        }

        val candidates = repository.findBetween(from, searchUntil)

        // 1. Check if we can place the appointment immediately at "from"
        if (candidates.isEmpty()) {
            val end = from.plus(duration)
            if (searchUntil == null || end <= searchUntil) {
                return from
            }
            throw NoFreeSlotAvailableException(
                "No free slot of ${duration.toMinutes()} minutes available before $searchUntil"
            )
        }

        // 2. Before the first appointment
        val first = candidates.first()
        val earliestEnd = from.plus(duration)

        if (earliestEnd <= first.start) {
            return from
        }

        // 3. Between appointments
        for (i in 0 until candidates.size - 1) {
            val current = candidates[i]
            val next = candidates[i + 1]

            val gapStart = current.end
            val gapEnd = gapStart.plus(duration)

            if (gapEnd <= next.start) {
                if (searchUntil == null || gapEnd <= searchUntil) {
                    return gapStart
                }
            }
        }

        // 4. After the last appointment
        val lastEnd = candidates.last().end
        val finalEnd = lastEnd.plus(duration)

        if (searchUntil == null || finalEnd <= searchUntil) {
            return lastEnd
        }

        throw NoFreeSlotAvailableException(
            "No free slot of ${duration.toMinutes()} minutes available before $searchUntil"
        )
    }

}
