package com.clianz;

import com.codahale.metrics.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@RestController
public class HelloController {

    public HelloController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private MeterRegistry meterRegistry;

//    @Autowired
//    public HelloController(MetricRegistry metricRegistry) {
//        this.metricRegistry = metricRegistry;
//    }

//    @PostConstruct
//    public void setupReg() {
//        timer = metricRegistry.timer("calcTimer");
//        meter = metricRegistry.meter("calcMeter");
//    }
//
//    private MetricRegistry metricRegistry;
//    private Timer timer;
//    private Meter meter;

//    private Counter mCounter = meterRegistry.counter("calc.microCounter", "uri", "/messages");
//    private io.micrometer.core.instrument.Timer mIimer = meterRegistry.timer("calc.microTimer", "uri", "/messages");
    private Counter mCounter;
    private io.micrometer.core.instrument.Timer mIimer;

    @PostConstruct
    public void setupReg() {
        mCounter = meterRegistry.counter("calc.microCounter", "uri", "/messages");
        mIimer = meterRegistry.timer("calc.microTimer", "uri", "/messages");
    }

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    String hello() {
        return "Hello World!";
    }

    @Data
    static class Result {
        private final int left;
        private final int right;
        private final long answer;
    }

    // SQL sample
//    @Timed
    @RequestMapping("calc")
    Result calc(@RequestParam int left, @RequestParam int right) {
//        Timer.Context context = timer.time();
//        meter.mark();
        mCounter.increment();
        long now = System.currentTimeMillis();

        try {
            MapSqlParameterSource source = new MapSqlParameterSource()
                    .addValue("left", left)
                    .addValue("right", right);
            return jdbcTemplate.queryForObject("SELECT :left + :right AS answer", source,
                    (rs, rowNum) -> new Result(left, right, rs.getLong("answer")));
        } finally {
            mIimer.record(System.currentTimeMillis() - now, TimeUnit.MILLISECONDS);
//            context.stop();
        }
    }

}
