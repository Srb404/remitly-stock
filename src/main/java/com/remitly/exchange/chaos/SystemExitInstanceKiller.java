package com.remitly.exchange.chaos;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SystemExitInstanceKiller implements InstanceKiller {

    private static final Logger log = LoggerFactory.getLogger(SystemExitInstanceKiller.class);
    private static final long EXIT_DELAY_MS = 100L;
    private static final int EXIT_CODE = 1;

    private final ApplicationContext applicationContext;

    public SystemExitInstanceKiller(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void scheduleKill() {
        log.warn("Chaos endpoint invoked — this instance will exit in {} ms", EXIT_DELAY_MS);
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(EXIT_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            int code = SpringApplication.exit(applicationContext, () -> EXIT_CODE);
            System.exit(code);
        });
    }
}