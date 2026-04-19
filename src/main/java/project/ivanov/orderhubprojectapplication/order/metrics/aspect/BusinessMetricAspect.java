package project.ivanov.orderhubprojectapplication.order.metrics.aspect;


import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import project.ivanov.orderhubprojectapplication.order.metrics.annotation.BusinessMetric;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessMetricAspect {

    private final MeterRegistry meterRegistry;

    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();


    @Around("@annotation(metric)")
    public Object measure(
            ProceedingJoinPoint joinPoint,
            BusinessMetric metric
    ) throws Throwable {

        long start = System.nanoTime();

        String status = "success";

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            status = "error";
            throw e;
        } finally {
            long duration = System.nanoTime() - start;

            recordMetrics(metric, joinPoint, duration, status);
        }

    }

    private void recordMetrics(
            BusinessMetric metric,
            ProceedingJoinPoint joinPoint,
            long duration,
            String status
    ) {

        try{
            String metricName = metric.value();

            String className = joinPoint.getTarget().getClass().getSimpleName();

            Tags allTags = Tags.of("class", className, "status", status);

            allTags = addCustomTags(allTags, metric.tags());

            Counter.builder("%s.total".formatted(metricName))
                    .tags(allTags)
                    .description("total calls")
                    .register(meterRegistry)
                    .increment();

            String timerKey = buildTimerKey(metricName, className, metric.tags(), status);

            Timer timer = timers.computeIfAbsent(timerKey, key ->{
                        Tags timerTags = Tags.of("class", className, "status", status);

                        timerTags = addCustomTags(timerTags, metric.tags());

                        return Timer.builder("%s.duration".formatted(metricName))
                                .tags(timerTags)
                                .description("Execution duration")
                                .publishPercentileHistogram()
                                .register(meterRegistry);
                    }
            );

            timer.record(duration, TimeUnit.NANOSECONDS);
        } catch (Exception e){
            log.warn("Metric was wrote with error: {}", e.getMessage());
        }
    }

    private String buildTimerKey(
            String metricName,
            String className,
            String[] tags,
            String status
    ) {

        StringBuilder timerKey = new StringBuilder();
        timerKey.append(metricName).append(".").append(className);

        if (tags != null || tags.length > 0) {

            String[] sorted = tags.clone();

            Arrays.sort(sorted);

            for (String tag : sorted) {
                timerKey.append(".").append(tag);
            }
        }

        timerKey.append(".").append(status);

        return timerKey.toString();
    }

    private Tags addCustomTags(
            Tags allTags,
            String[] tags
    ) {
        if (tags == null || tags.length == 0) {
            return allTags;
        }

        Tags result = allTags;

        for (String tag : tags) {

            int index = tag.indexOf('=');

            if (index > 1) {
                String key = tag.substring(0, index).trim();
                String value = tag.substring(index + 1);

                result = result.and(key, value);
            }
        }

        return result;
    }
}
