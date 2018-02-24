package com.clianz;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.micrometer.core.instrument.Metrics;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class HelloController {

    @Autowired
    public HelloController(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @PostConstruct
    public void setupReg() {
        timer = metricRegistry.timer("calcTimer");
        meter = metricRegistry.meter("calcMeter");
    }

    private MetricRegistry metricRegistry;

    //    private Timer timer = metricRegistry.timer(name(HelloController.class, "responses"));
//    private Timer timer = metricRegistry.timer("calcTimer");
    private Timer timer;

//    private Meter meter = metricRegistry.meter(name(HelloController.class, "responseCount"));
//    private Meter meter = metricRegistry.meter("calcMeter");
    private Meter meter;

    private io.micrometer.core.instrument.Counter counter = Metrics.counter("calc.calls", "uri", "/messages");

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
        Timer.Context context = timer.time();
        meter.mark();
        counter.increment();
        try {
            MapSqlParameterSource source = new MapSqlParameterSource()
                    .addValue("left", left)
                    .addValue("right", right);
            return jdbcTemplate.queryForObject("SELECT :left + :right AS answer", source,
                    (rs, rowNum) -> new Result(left, right, rs.getLong("answer")));
        } finally {
            context.stop();
        }
    }

}
