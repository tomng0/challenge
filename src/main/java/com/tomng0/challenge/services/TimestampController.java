package com.tomng0.challenge.services;

import java.math.BigInteger;

import com.tomng0.challenge.model.Timestamp;
import com.tomng0.challenge.model.Now;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "timestamp")
@RestController
public class TimestampController {
    private volatile BigInteger calls = BigInteger.valueOf(0);

    @RequestMapping(method = RequestMethod.GET, path = "now", headers = "Accept=application/json")
    public synchronized Timestamp getNow() {
        this.calls = this.calls.add(BigInteger.ONE);
        return new Now(this.calls);
    }
}