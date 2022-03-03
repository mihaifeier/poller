package com.kry.poller.verticle;

import com.kry.poller.service.StoreService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class StoreVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(StoreVerticle.class);
    private StoreService storeService;

    @Override
    public void start(Promise<Void> startPromise) {
        storeService = new StoreService(vertx);

        vertx.eventBus().<String>consumer("polling.get", message -> storeService.getPolls(message));
        vertx.eventBus().<JsonObject>consumer("polling.add", message -> storeService.addPoll(message));
        vertx.eventBus().<JsonObject>consumer(
            "polling.update",
            message -> storeService.updatePoll(message, false)
        );
        vertx.eventBus().<JsonObject>consumer(
            "polling.update-status",
            message -> storeService.updatePoll(message, true)
        );
        vertx.eventBus().<String>consumer("polling.delete", message -> storeService.deletePoll(message));

        logger.info("Store Verticle started successfully.");
        startPromise.complete();
    }


}
