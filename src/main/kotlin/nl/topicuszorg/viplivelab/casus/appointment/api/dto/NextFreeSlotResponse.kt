package nl.topicuszorg.viplivelab.casus.appointment.api.dto


import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

@Schema(description = "Response containing the next available slot.")
data class NextFreeSlotResponse(

    @Schema(
        description = "The start time of the next available free slot.",
        example = "2025-01-10T11:30:00+01:00[Europe/Amsterdam]"
    )
    val start: ZonedDateTime
)
