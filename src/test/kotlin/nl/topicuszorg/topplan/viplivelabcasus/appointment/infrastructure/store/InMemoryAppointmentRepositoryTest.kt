package nl.topicuszorg.topplan.viplivelabcasus.appointment.infrastructure.store

import nl.topicuszorg.viplivelab.casus.appointment.domain.model.Appointment
import nl.topicuszorg.viplivelab.casus.appointment.infrastructure.InMemoryAppointmentRepository
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class InMemoryAppointmentRepositoryTest {

    private lateinit var repository: InMemoryAppointmentRepository
    private val zone: ZoneId = ZoneId.of("Europe/Amsterdam")

    @BeforeEach
    fun setUp() {
        // Arrange: fresh repository for each test to avoid cross-test interference
        repository = InMemoryAppointmentRepository()
    }

    /**
     * Creates a ZonedDateTime at the given moment in the test's fixed zone.
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
    fun `save keeps appointments sorted by start time`() {
        // arrange
        val a2 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 9, 0),
            end = zdt(2025, 1, 1, 9, 30),
            description = "Earliest"
        )
        val a1 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 10, 0),
            end = zdt(2025, 1, 1, 11, 0),
            description = "Middle"
        )
        val a3 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 12, 0),
            end = zdt(2025, 1, 1, 13, 0),
            description = "Latest"
        )

        // act – intentionally save in non-sorted order
        repository.save(a1)
        repository.save(a3)
        repository.save(a2)

        val allFromMorning = repository.findBetween(
            start = zdt(2025, 1, 1, 0, 0),
            end = null
        )

        // assert
        assertEquals(3, allFromMorning.size)
        assertEquals(a2.id, allFromMorning[0].id) // 09:00
        assertEquals(a1.id, allFromMorning[1].id) // 10:00
        assertEquals(a3.id, allFromMorning[2].id) // 12:00
    }

    @Test
    fun `findOverlapping returns empty list when there is no overlap`() {
        // arrange
        val a1 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 9, 0),
            end = zdt(2025, 1, 1, 10, 0)
        )
        repository.save(a1)

        // act
        val result = repository.findOverlapping(
            start = zdt(2025, 1, 1, 10, 0),
            end = zdt(2025, 1, 1, 11, 0)
        )

        // assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findOverlapping returns appointments that overlap given range`() {
        // arrange
        val a1 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 9, 0),
            end = zdt(2025, 1, 1, 10, 0)
        )
        val a2 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 9, 30),
            end = zdt(2025, 1, 1, 10, 30)
        )
        repository.save(a1)
        repository.save(a2)

        // act
        val result = repository.findOverlapping(
            start = zdt(2025, 1, 1, 9, 45),
            end = zdt(2025, 1, 1, 10, 15)
        )

        // assert
        assertEquals(2, result.size)
        val ids = result.map { it.id }.toSet()
        assertTrue(ids.contains(a1.id))
        assertTrue(ids.contains(a2.id))
    }

    @Test
    fun `findBetween without end returns all appointments starting from given time`() {
        // arrange
        val a1 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 8, 0),
            end = zdt(2025, 1, 1, 9, 0)
        )
        val a2 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 10, 0),
            end = zdt(2025, 1, 1, 11, 0)
        )
        repository.save(a1)
        repository.save(a2)

        // act
        val result = repository.findBetween(
            start = zdt(2025, 1, 1, 9, 0),
            end = null
        )

        // assert
        assertEquals(1, result.size)
        assertEquals(a2.id, result[0].id)
    }

    @Test
    fun `findBetween with end returns only appointments in window`() {
        // arrange
        val a1 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 8, 0),
            end = zdt(2025, 1, 1, 9, 0)
        )
        val a2 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 9, 30),
            end = zdt(2025, 1, 1, 10, 0)
        )
        val a3 = Appointment(
            id = UUID.randomUUID(),
            start = zdt(2025, 1, 1, 11, 0),
            end = zdt(2025, 1, 1, 12, 0)
        )
        repository.save(a1)
        repository.save(a2)
        repository.save(a3)

        // act
        val result = repository.findBetween(
            start = zdt(2025, 1, 1, 9, 0),
            end = zdt(2025, 1, 1, 11, 0)
        )
        println("DEBUG result: $result")   // easiest debugging right here


        // assert
        assertEquals(1, result.size)
        assertEquals(a2.id, result[0].id)
    }
}
