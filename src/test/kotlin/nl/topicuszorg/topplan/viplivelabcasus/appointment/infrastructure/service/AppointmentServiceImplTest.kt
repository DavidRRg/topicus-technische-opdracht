package nl.topicuszorg.topplan.viplivelabcasus.appointment.infrastructure.service

import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.AppointmentConflictException
import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.InvalidAppointmentTimeException
import nl.topicuszorg.viplivelab.casus.appointment.domain.exception.NoFreeSlotAvailableException
import nl.topicuszorg.viplivelab.casus.appointment.domain.port.AppointmentRepository
import nl.topicuszorg.viplivelab.casus.appointment.domain.service.AppointmentService
import nl.topicuszorg.viplivelab.casus.appointment.infrastructure.InMemoryAppointmentRepository
import nl.topicuszorg.viplivelab.casus.appointment.infrastructure.service.AppointmentServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.test.*

class AppointmentServiceImplTest {

    private val zone: ZoneId = ZoneId.of("Europe/Amsterdam")

    private lateinit var repository: AppointmentRepository
    private lateinit var service: AppointmentService

    @BeforeEach
    fun setUp() {
        // Arrange: fresh repository and service for each test to avoid cross-test interference
        repository = InMemoryAppointmentRepository()
        service = AppointmentServiceImpl(repository)
    }

    /**
     * Helper to construct a ZonedDateTime in a fixed zone.
     */
    private fun zdt(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int = 0
    ): ZonedDateTime =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)

    @Test
    fun `createAppointment succeeds when there is no overlap`() {
        // Arrange
        val start = zdt(2025, 1, 1, 9, 0)
        val end = zdt(2025, 1, 1, 10, 0)

        // Act
        val created = service.createAppointment(start, end, description = "Intake")

        // Assert
        assertEquals(start, created.start)
        assertEquals(end, created.end)
        assertEquals("Intake", created.description)
        assertTrue(created.id is UUID)

        val all = repository.findBetween(zdt(2025, 1, 1, 0, 0), null)
        assertEquals(1, all.size)
        assertEquals(created.id, all.first().id)
    }

    @Test
    fun `createAppointment throws InvalidAppointmentTimeException when start is not before end`() {
        // Arrange
        val start = zdt(2025, 1, 1, 10, 0)
        val end = zdt(2025, 1, 1, 10, 0) // equal, so invalid

        // Act + Assert
        assertFailsWith<InvalidAppointmentTimeException> {
            service.createAppointment(start, end, description = "Invalid")
        }
    }

    @Test
    fun `createAppointment throws AppointmentConflictException when there is an overlap`() {
        // Arrange
        service.createAppointment(
            start = zdt(2025, 1, 1, 9, 0),
            end = zdt(2025, 1, 1, 10, 0),
            description = "Existing"
        )

        val overlappingStart = zdt(2025, 1, 1, 9, 30)
        val overlappingEnd = zdt(2025, 1, 1, 9, 45)

        // Act + Assert
        assertFailsWith<AppointmentConflictException> {
            service.createAppointment(overlappingStart, overlappingEnd, description = "Overlap")
        }
    }

    @Test
    fun `createAppointment allows back-to-back appointments without overlap`() {
        // Arrange
        val first = service.createAppointment(
            start = zdt(2025, 1, 1, 9, 0),
            end = zdt(2025, 1, 1, 10, 0),
            description = "First"
        )

        // Act
        val second = service.createAppointment(
            start = zdt(2025, 1, 1, 10, 0),  // exactly at end of first
            end = zdt(2025, 1, 1, 11, 0),
            description = "Second"
        )

        // Assert
        assertEquals(zdt(2025, 1, 1, 10, 0), second.start)
        assertEquals(zdt(2025, 1, 1, 11, 0), second.end)

        val all = repository.findBetween(zdt(2025, 1, 1, 0, 0), null)
        assertTrue(all.any { it.id == first.id })
        assertTrue(all.any { it.id == second.id })
    }

    @Test
    fun `findNextFreeSlot returns from when there are no appointments`() {
        // Arrange
        val from = zdt(2025, 1, 2, 9, 0)
        val duration = Duration.ofMinutes(30)

        // Act
        val slot = service.findNextFreeSlot(from, duration, searchUntil = null)

        // Assert
        assertEquals(from, slot)
    }

    @Test
    fun `findNextFreeSlot skips over existing appointment and returns next gap`() {
        // Arrange
        val busyStart = zdt(2025, 1, 3, 9, 0)
        val busyEnd = zdt(2025, 1, 3, 10, 0)
        service.createAppointment(busyStart, busyEnd, description = "Consult")

        val from = zdt(2025, 1, 3, 9, 0)
        val duration = Duration.ofMinutes(30)

        // Act
        val slot = service.findNextFreeSlot(from, duration, searchUntil = null)

        // Assert
        assertEquals(busyEnd, slot)
    }

    @Test
    fun `findNextFreeSlot throws NoFreeSlotAvailableException when nothing fits before searchUntil`() {
        // Arrange
        service.createAppointment(
            start = zdt(2025, 1, 4, 9, 0),
            end = zdt(2025, 1, 4, 10, 0),
            description = "Block 1"
        )
        service.createAppointment(
            start = zdt(2025, 1, 4, 10, 0),
            end = zdt(2025, 1, 4, 11, 0),
            description = "Block 2"
        )

        val from = zdt(2025, 1, 4, 9, 0)
        val duration = Duration.ofMinutes(60)
        val searchUntil = zdt(2025, 1, 4, 11, 0)

        // Act + Assert
        assertFailsWith<NoFreeSlotAvailableException> {
            service.findNextFreeSlot(from, duration, searchUntil)
        }
    }

    @Test
    fun `findNextFreeSlot finds a slot inside a gap before searchUntil`() {
        // Arrange
        // Busy from 10:00 to 11:00, search window stops at 11:30
        service.createAppointment(
            start = zdt(2025, 1, 5, 10, 0),
            end = zdt(2025, 1, 5, 11, 0),
            description = "Block"
        )

        val from = zdt(2025, 1, 5, 9, 0)
        val duration = Duration.ofMinutes(30)
        val searchUntil = zdt(2025, 1, 5, 11, 30)

        // Act
        val slot = service.findNextFreeSlot(from, duration, searchUntil)

        // Assert
        // Earliest slot is 09:00–09:30, which fits fully before searchUntil
        assertEquals(zdt(2025, 1, 5, 9, 0), slot)
    }

    @Test
    fun `findNextFreeSlot prefers earliest gap when multiple are available`() {
        // Arrange
        // Appointments: 09–10, 12–13. Look for 30 minutes starting at 08:00.
        service.createAppointment(
            start = zdt(2025, 1, 6, 9, 0),
            end = zdt(2025, 1, 6, 10, 0),
            description = "A"
        )
        service.createAppointment(
            start = zdt(2025, 1, 6, 12, 0),
            end = zdt(2025, 1, 6, 13, 0),
            description = "B"
        )

        val from = zdt(2025, 1, 6, 8, 0)
        val duration = Duration.ofMinutes(30)

        // Act
        val slot = service.findNextFreeSlot(from, duration, searchUntil = null)

        // Assert
        // The earliest free gap is 08:00–09:00, so slot should be 08:00.
        assertEquals(zdt(2025, 1, 6, 8, 0), slot)
    }

    @Test
    fun `findNextFreeSlot finds slot immediately after last appointment`() {
        // Arrange
        service.createAppointment(
            start = zdt(2025, 1, 7, 9, 0),
            end = zdt(2025, 1, 7, 10, 0),
            description = "Block"
        )

        val from = zdt(2025, 1, 7, 10, 0)
        val duration = Duration.ofMinutes(30)

        // Act
        val slot = service.findNextFreeSlot(from, duration, searchUntil = null)

        // Assert
        assertEquals(zdt(2025, 1, 7, 10, 0), slot)
    }

    @Test
    fun `findNextFreeSlot throws for zero or negative duration`() {
        // Arrange
        val from = zdt(2025, 1, 8, 9, 0)

        // Act + Assert
        assertFailsWith<IllegalArgumentException> {
            service.findNextFreeSlot(from, Duration.ZERO, searchUntil = null)
        }

        assertFailsWith<IllegalArgumentException> {
            service.findNextFreeSlot(from, Duration.ofMinutes(-15), searchUntil = null)
        }
    }
}
