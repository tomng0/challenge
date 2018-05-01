package com.tomng0.challenge.model;

import java.math.BigInteger;

public interface Timestamp {
    /**
     * the time in EST Timezone
     */
    String getTimestamp();

    /**
     * the number of times the service is called
     */
    BigInteger getCalls();
}