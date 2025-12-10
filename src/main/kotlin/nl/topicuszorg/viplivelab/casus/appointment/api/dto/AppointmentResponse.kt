package nl.topicuszorg.viplivelab.casus.appointment.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "Representation of a scheduled appointment.")
data class AppointmentResponse(

    @Schema(description = "Unique identifier of the appointment.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    val id: UUID,

    @Schema(
        description = "Start time of the appointment.",
        example = "2025-01-10T09:00:00+01:00[Europe/Amsterdam]"
    )
    val start: ZonedDateTime,

    @Schema(
        description = "End time of the appointment.",
        example = "2025-01-10T09:30:00+01:00[Europe/Amsterdam]"
    )
    val end: ZonedDateTime,

    @Schema(description = "Optional description.", example = "Intake gesprek")
    val description: String?
)