package event.domain.util

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

abstract class PublicationTargetIdentifierMixin
    @JsonCreator
    constructor(
        @JsonProperty("value") value: String,
    )