package com.demo.reactive.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class RepositoryTimingAspect {

    private final MeterRegistry registry;

    public RepositoryTimingAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    @Around("execution(* com.demo.reactive.repository.*.*(..))")
    public Object timeRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(registry);
        String operation = joinPoint.getSignature().getName();
        String repository = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnTerminate(() -> sample.stop(registry.timer("db.query", "repository", repository, "operation", operation)));
        } else if (result instanceof Flux) {
            return ((Flux<?>) result)
                .doOnTerminate(() -> sample.stop(registry.timer("db.query", "repository", repository, "operation", operation)));
        }
        
        return result;
    }
}
