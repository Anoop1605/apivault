package com.sentinel.policy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Condition — Simple POJO representing a single policy condition.
 * Not currently used as a separate entity, but defined for clarity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    private String type;
    private String operator;
    private Object value;
}
