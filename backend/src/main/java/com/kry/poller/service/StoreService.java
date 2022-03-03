package com.kry.poller.service;

import com.kry.poller.repository.PollRepository;
import com.kry.poller.util.PollMapper;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import static com.kry.poller.util.PollMapper.mapToJsonObject;
import static com.kry.poller.util.PollMapper.mapToPoll;

public class StoreService {
    private final Logger logger = LoggerFactory.getLogger(StoreService.class);
    private final Vertx vertx;
    private final PollRepository pollRepository;

    public StoreService(Vertx vertx) {
        this.vertx = vertx;
        this.pollRepository = new PollRepository(vertx);
    }


    public void getPolls(Message<String> message) {
        pollRepository.getPolls(message.body())
            .onSuccess(result -> {
                logger.info("Retrieved polls successfully from database.");
                message.reply(PollMapper.mapToJsonArray(result));
            })
            .onFailure(throwable -> {
                logger.error(throwable.getMessage());
                message.fail(500, throwable.getMessage());
            });
    }


    public void addPoll(Message<JsonObject> message) {
        pollRepository.addPoll(mapToPoll(message.body()))
            .onSuccess(poll -> {
                logger.info("Added poll with id: " + poll.getId());
                message.reply(mapToJsonObject(poll));
            })
            .onFailure(throwable -> {
                logger.error(throwable.getMessage());
                message.fail(500, throwable.getMessage());
            });
    }

    public void updatePoll(Message<JsonObject> message, Boolean statusUpdate) {
        pollRepository.updatePoll(mapToPoll(message.body()))
            .onSuccess(poll -> {
                JsonObject mappedPoll = mapToJsonObject(poll);
                logger.info("Updated poll with id: " + poll.getId());
                if (statusUpdate) {
                    vertx.eventBus().send("polling.live-update", mappedPoll);
                }
                message.reply(mappedPoll);
            })
            .onFailure(throwable -> {
                logger.error(throwable.getMessage());
                message.fail(500, throwable.getMessage());
            });
    }

    public void deletePoll(Message<String> message) {
        String id = message.body();
        pollRepository.deletePoll(id)
            .onSuccess(reply -> {
                logger.info("Deleted poll with id: " + id);
                message.reply("Success");
            })
            .onFailure(throwable -> {
                logger.error(throwable.getMessage());
                message.fail(500, throwable.getMessage());
            });
    }
}
