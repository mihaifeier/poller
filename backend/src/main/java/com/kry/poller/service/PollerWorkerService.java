package com.kry.poller.service;

import com.kry.poller.model.Poll;
import com.kry.poller.model.enums.PollStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import static com.kry.poller.util.PollMapper.mapToJsonObject;
import static com.kry.poller.util.PollMapper.mapToPoll;

public class PollerWorkerService {
    private final Vertx vertx;
    private final WebClient client;

    public PollerWorkerService(Vertx vertx) {
        this.vertx = vertx;
        client = WebClient.create(vertx, new WebClientOptions().setConnectTimeout(5000).setKeepAliveTimeout(5));
    }

    public void handlePollingRequest(Message<JsonObject> message) {
        Poll poll = mapToPoll(message.body());
        client.getAbs(poll.getUrl())
            .send()
            .onComplete(response -> {
                PollStatus status = PollStatus.FAIL;
                if (response.succeeded() && response.result().statusCode() == 200) {
                    status = PollStatus.OK;
                }

                if (!poll.getPollStatus().equals(status)) {
                    poll.setPollStatus(status);
                    vertx.eventBus().send(
                        "polling.update-status", mapToJsonObject(poll));
                }
            });
    }
}
