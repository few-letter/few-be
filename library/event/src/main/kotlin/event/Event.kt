package event

import java.time.LocalDateTime

/**
 * Event
 *
 * 이벤트
 *
 * @property eventId 이벤트 식별자
 * @property eventType 이벤트 행위 타입
 * @property eventTime 이벤트 발행 시간 (기본값: 현재 시간)
 */
abstract class Event(
    val eventId: String = EventUtils.generateEventId(),
    val eventType: String,
    val eventTime: LocalDateTime = LocalDateTime.now(),
)