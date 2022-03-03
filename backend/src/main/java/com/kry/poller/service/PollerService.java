package com.kry.poller.service;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;

public class PollerService {
    private final Logger logger = LoggerFactory.getLogger(PollerService.class);
    private final Vertx vertx;

    public PollerService(Vertx vertx) {
        this.vertx = vertx;
    }

    public void requestPoll() {
        logger.info("Requesting polling");

        vertx.eventBus().<JsonArray>request("polling.get", null, asyncResult -> {
            if (asyncResult.succeeded()) {
                asyncResult.result().body().forEach(poll -> vertx.eventBus().send("polling.request", poll));
            } else {
                logger.error(asyncResult.cause().getMessage());
            }
        });
    }
}
