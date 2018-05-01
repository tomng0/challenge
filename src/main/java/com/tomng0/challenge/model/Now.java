package com.tomng0.challenge.model;

import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class Now implements Timestamp {

    private final BigInteger calls;

    @JsonProperty(access = Access.READ_ONLY)
    public String getTimestamp() {
        return OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("EST", ZoneId.SHORT_IDS)).toString();
    }

    public BigInteger getCalls() {
        return this.calls;
    }

    public Now() {
        this(BigInteger.ONE);
    }

    public Now(BigInteger calls) {
        this.calls = calls;
    }

    public String toString() {
        return "Timestamp(" + this.getTimestamp() + ", " + this.calls + ")";
    }
}