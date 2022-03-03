package com.kry.poller.verticle;

import com.kry.poller.service.PollerWorkerService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class PollerWorkerVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(PollerWorkerVerticle.class);
    private PollerWorkerService pollerWorkerService;

    @Override
    public void start(Promise<Void> startPromise) {
        pollerWorkerService = new PollerWorkerService(vertx);
        vertx.eventBus().<JsonObject>consumer(
            "polling.request",
            message -> pollerWorkerService.handlePollingRequest(message)
        );

        logger.info("Poller Worker Verticle started successfully.");

        startPromise.complete();
    }
}
