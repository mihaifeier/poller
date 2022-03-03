package com.kry.poller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kry.poller.verticle.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.jackson.DatabindCodec;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new JavaTimeModule());

        ObjectMapper prettyMapper = DatabindCodec.prettyMapper();
        prettyMapper.registerModule(new JavaTimeModule());

        vertx.deployVerticle(new PollerVerticle());
        vertx.deployVerticle(new PollerWorkerVerticle());
        vertx.deployVerticle(new StoreVerticle());
        vertx.deployVerticle(new GatewayVerticle());

        startPromise.complete();
    }
}
