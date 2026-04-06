package com.sentinel.policy.rules;

import com.sentinel.policy.model.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Condition that checks if request source IP is in whitelist/blacklist
 * TODO: Implement CIDR notation support for IP ranges
 */
@Data
@Builder
@AllArgsConstructor
public class IPCondition implements Condition {
    private List<String> allowedIPs;
    private boolean isWhitelist; // true = whitelist, false = blacklist

    @Override
    public boolean evaluate(Map<String, Object> attributes) {
        Object sourceIP = attributes.get("sourceIP");
        if (sourceIP == null) {
            return false;
        }

        String ip = sourceIP.toString();
        boolean isInList = allowedIPs.contains(ip);

        return isWhitelist ? isInList : !isInList;
    }

    @Override
    public String getType() {
        return "IP";
    }
}
