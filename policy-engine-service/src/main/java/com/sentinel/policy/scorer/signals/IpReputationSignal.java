package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IpReputationSignal — Evaluates risk based on the source IP address.
 *
 * Tracks known-suspicious IPs seen across requests. In a production system,
 * this would integrate with external threat intelligence feeds.
 * For now, it uses a dynamically built in-memory reputation table.
 *
 * Risk factors:
 *  - Private/loopback IPs get low risk (trusted internal traffic)
 *  - Unknown external IPs get moderate baseline risk
 *  - IPs that repeatedly trigger denies get elevated risk
 */
@Component
public class IpReputationSignal implements RiskSignal {

    /** IPs that have been flagged by previous policy denials. */
    private final Set<String> flaggedIps = ConcurrentHashMap.newKeySet();

    public IpReputationSignal() {
        // Seed with some "Known Bad" IPs for demo purposes
        flaggedIps.add("1.2.3.4");
        flaggedIps.add("99.88.77.66");
    }

    @Override
    public double evaluate(RequestContext ctx) {
        String ip = ctx.getSourceIp();
        if (ip == null || ip.isEmpty()) {
            return 0.3; // Unknown source — moderate risk
        }

        // Loopback and private ranges are low risk
        if (ip.startsWith("127.") || ip.startsWith("10.") ||
                ip.startsWith("192.168.") || ip.startsWith("172.16.") ||
                ip.equals("0:0:0:0:0:0:0:1")) {
            return 0.05;
        }

        // Previously flagged IPs carry elevated risk
        if (flaggedIps.contains(ip)) {
            return 0.8;
        }

        // Default external IP risk
        return 0.2;
    }

    /**
     * Called by other components when a request from this IP is denied,
     * allowing the signal to learn over time.
     */
    public void flagIp(String ip) {
        if (ip != null) {
            flaggedIps.add(ip);
        }
    }

    @Override
    public String name() {
        return "ip_reputation";
    }

    @Override
    public double weight() {
        return 1.5;
    }
}
