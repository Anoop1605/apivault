package com.sentinel.policy.rules;

import com.sentinel.policy.model.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Condition that checks if request timestamp falls within allowed time window
 * TODO: Support timezone-aware time windows and recurring schedules
 */
@Data
@Builder
@AllArgsConstructor
public class TimeCondition implements Condition {
    private int startHour; // 0-23
    private int endHour;   // 0-23

    @Override
    public boolean evaluate(Map<String, Object> attributes) {
        Object timestamp = attributes.get("timestamp");
        if (timestamp == null) {
            return false;
        }

        LocalDateTime time = (timestamp instanceof LocalDateTime)
            ? (LocalDateTime) timestamp
            : LocalDateTime.now();

        int hour = time.getHour();
        return hour >= startHour && hour < endHour;
    }

    @Override
    public String getType() {
        return "TIME";
    }
}
