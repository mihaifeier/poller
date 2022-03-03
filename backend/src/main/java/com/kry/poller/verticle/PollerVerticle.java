package com.kry.poller.verticle;

import com.kry.poller.service.PollerService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class PollerVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(PollerVerticle.class);
    private PollerService pollerService;

    @Override
    public void start(Promise<Void> startPromise) {
        pollerService = new PollerService(vertx);
        vertx.setPeriodic(5000, tId -> pollerService.requestPoll());

        logger.info("Poller Verticle started successfully.");
        startPromise.complete();
    }
}
