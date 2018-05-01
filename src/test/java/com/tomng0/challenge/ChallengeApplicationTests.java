package com.tomng0.challenge;

import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.Phaser;

import com.tomng0.challenge.model.Now;

import org.assertj.core.data.Offset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ChallengeApplicationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getTimestampNowURL() {
        return "http://localhost:" + port + "/timestamp/now";
    }

    /**
     * returns the next call to the API at "/timestamp/now"
     */
    private Now nextNow() {
        return restTemplate.getForObject(this.getTimestampNowURL(), Now.class);
    }

    private BigInteger nextCalls() {
        return nextNow().getCalls();
    }

    private String nextTimestamp() {
        return nextNow().getTimestamp();
    }

    /**
     * ensures that the API not returns an empty string
     */
    @Test
    public void shouldReturnSomething() {
        assertThat(Optional.ofNullable(restTemplate.getForObject(this.getTimestampNowURL(), String.class)).orElse("")
                .length() > 0);
    }

    /**
     * ensures that the API is serializable to a timestamp Now instance
     */
    @Test
    public void shouldReturnsSerializableNowTimestamp() {
        assertThatCode(() -> restTemplate.getForObject(this.getTimestampNowURL(), Now.class))
                .doesNotThrowAnyException();
    }

    /**
     * Ensures that the API has calls incremented properly between 2 consecutive calls
     */
    @Test
    public void shouldIncrementCallsBetweenAPICall() {
        final BigInteger curr = this.nextCalls();
        final BigInteger next = this.nextCalls();
        assertThat(next.compareTo(curr.add(BigInteger.ONE)))
                .as("should increment calls field between API call, initially %s, got %s after the next API call", curr,
                        next)
                .isEqualTo(0);
    }

    /**
     * Ensures that the API has calls incremented properly even with concurrent
     * modification
     */
    @Test
    public void shouldIncrementCallsConcurrently() {
        final int TRIES = 10000;
        final Phaser phaser = new Phaser(1);
        final BigInteger first = this.nextCalls();
        for (int i = 0; i < TRIES; i++) {
            phaser.register();
            new Thread(() -> {
                this.nextCalls(); // just make an http call
                phaser.arriveAndDeregister();
            }).start();
        }
        // joins all
        phaser.arriveAndAwaitAdvance();
        final BigInteger last = this.nextCalls();
        assertThat(last.compareTo(first.add(BigInteger.valueOf(TRIES + 1)))).as(
                "should increment calls between concurrent API calls, initial value %s, after %d concurrent API calls, got %s",
                first, TRIES, last).isEqualTo(0);
    }

    /**
     * should the timestamp is a valid timestamp 
     */
    @Test
    public void shouldTimestampAValidISOTimestamp() {
        assertThatCode(() -> {
            OffsetDateTime.parse(nextTimestamp(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }).as("expect timestamp to be a valid ISO8601 format").doesNotThrowAnyException();
    }

    /**
     * Should the timestamp has EST's timezone
     */
    @Test
    public void shouldTimestampInEST() {
        final OffsetDateTime got = OffsetDateTime.parse(nextTimestamp());
        final OffsetDateTime expected = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("EST", ZoneId.SHORT_IDS));

        final ZoneId expectedOffset = expected.getOffset().normalized();
        final ZoneId gotOffset = got.getOffset().normalized();
        assertThat(gotOffset)
                .as("expect time zone to be EST(\"-05:00\"), expected %s, got %s", expectedOffset, gotOffset)
                .isEqualTo(expectedOffset);
        assertThat(got.getYear()).as("expect same year").isEqualTo(expected.getYear());
        assertThat(got.getMonth()).as("expect same month").isEqualTo(expected.getMonth());
        assertThat(got.getDayOfMonth()).isEqualTo(expected.getDayOfMonth());
        assertThat(got.getHour()).isEqualTo(expected.getHour());
        assertThat(got.getMinute()).isEqualTo(expected.getMinute());
        assertThat(got.getSecond()).isCloseTo(expected.getSecond(), Offset.offset(10));
    }

}