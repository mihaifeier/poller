package com.kry.poller.service;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

import static com.kry.poller.util.Validator.validateUrl;

public class GatewayService {
    private final Logger logger = LoggerFactory.getLogger(GatewayService.class);
    private final Vertx vertx;
    private final Map<String, String> handlersByUser;

    public GatewayService(Vertx vertx) {
        this.vertx = vertx;
        handlersByUser = new HashMap<>();
    }

    public void getPolls(RoutingContext context) {
        String user = context.request().headers().get("user");

        vertx.eventBus().<JsonArray>request("polling.get", user, asyncResult -> {
            if (asyncResult.succeeded()) {
                logger.info("Get request successful.");
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .end(asyncResult.result().body().toString());
            } else {
                logger.error(asyncResult.cause().getMessage());
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", asyncResult.cause().getMessage()).toString());
            }
        });
    }

    public void addPoll(RoutingContext context) {
        JsonObject poll = context.getBodyAsJson();

        if (!validateUrl(poll.getString("url"))) {
            context.response()
                .putHeader("Content-Type", "application/json")
                .setStatusCode(400)
                .end(new JsonObject().put("error", "Cannot add Poll. URL is invalid.").toString());

            return;
        }

        vertx.eventBus().request("polling.add", poll, asyncResult -> {
            if (asyncResult.succeeded()) {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(201)
                    .end(asyncResult.result().body().toString());
            } else {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", asyncResult.cause().getMessage()).toString());
            }
        });
    }

    public void updatePoll(RoutingContext context) {
        JsonObject poll = context.getBodyAsJson();
        if (!validateUrl(poll.getString("url"))) {
            context.response()
                .putHeader("Content-Type", "application/json")
                .setStatusCode(400)
                .end(new JsonObject().put("error", "Cannot update Poll. URL is invalid.").toString());

            return;
        }

        vertx.eventBus().request("polling.update", poll, asyncResult -> {
            if (asyncResult.succeeded()) {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .end(asyncResult.result().body().toString());
            } else {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", asyncResult.cause().getMessage()).toString());
            }
        });
    }

    public void deletePoll(RoutingContext context) {
        String id = context.pathParam("id");

        vertx.eventBus().request("polling.delete", id, asyncResult -> {
            if (asyncResult.succeeded()) {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(204)
                    .end();
            } else {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", asyncResult.cause().getMessage()).toString());
            }
        });
    }

    public void handleLiveUpdateMessage(JsonObject poll) {
        String writeHandlerId = handlersByUser.get(poll.getString("user"));

        if (writeHandlerId != null) {
            logger.info("Sent live update.");
            vertx.eventBus().publish(writeHandlerId, poll.toString());
        }
    }

    public void handleSockJs(String user, String handlerId) {
        handlersByUser.put(user, handlerId);
    }
}
