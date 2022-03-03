package com.kry.poller.repository;

import com.kry.poller.model.Poll;
import com.kry.poller.model.enums.PollStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.kry.poller.util.PollMapper.mapToList;

public class PollRepository {
    private final String DB_HOST = System.getenv().getOrDefault("DATABASE", "127.0.0.1");
    private final Integer DB_PORT = Integer.valueOf(
        System.getenv().getOrDefault("PORT", "3309")
    );
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Vertx vertx;
    private MySQLPool client;


    public PollRepository(Vertx vertx) {
        this.vertx = vertx;
        configureDatabaseConnection();
    }

    private void configureDatabaseConnection() {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
            .setPort(DB_PORT)
            .setHost(DB_HOST)
            .setDatabase("dev")
            .setUser("dev")
            .setPassword("secret");

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        client = MySQLPool.pool(vertx, connectOptions, poolOptions);
    }

    public Future<List<Poll>> getPolls(String user) {
        Promise<List<Poll>> promise = Promise.promise();
        if (user != null) {
            client.preparedQuery("SELECT * FROM poll WHERE user=?")
                .execute(Tuple.of(user), asyncResult -> {
                    if (asyncResult.succeeded()) {
                        promise.complete(mapToList(asyncResult.result()));
                    } else {
                        promise.fail(asyncResult.cause().getMessage());
                    }
                });
        } else {
            client.preparedQuery("SELECT * FROM poll")
                .execute(asyncResult -> {
                    if (asyncResult.succeeded()) {
                        promise.complete(mapToList(asyncResult.result()));
                    } else {
                        promise.fail(asyncResult.cause().getMessage());
                    }
                });
        }

        return promise.future();
    }

    public Future<Poll> addPoll(Poll poll) {
        LocalDateTime date = LocalDateTime.now();
        Promise<Poll> promise = Promise.promise();
        client.preparedQuery(
            "INSERT INTO poll (url, name, poll_status, user, created_at) VALUES (?, ?, 'FAIL', ?, ?)"
        ).execute(
            Tuple.of(poll.getUrl(), poll.getName(), poll.getUser(), dateTimeFormatter.format(LocalDateTime.now())),
            asyncResult -> {
                if (asyncResult.succeeded()) {
                    poll.setId(asyncResult.result().property(MySQLClient.LAST_INSERTED_ID));
                    poll.setPollStatus(PollStatus.FAIL);
                    poll.setCreatedAt(date);

                    promise.complete(poll);
                } else {
                    promise.fail(asyncResult.cause().getMessage());
                }
            });

        return promise.future();
    }

    public Future<Poll> updatePoll(Poll poll) {
        Promise<Poll> promise = Promise.promise();
        client.preparedQuery("UPDATE poll SET url=?, name=?, poll_status=? WHERE id=?")
            .execute(
                Tuple.of(poll.getUrl(), poll.getName(), poll.getPollStatus(), poll.getId()),
                asyncResult -> {
                    if (asyncResult.succeeded()) {
                        promise.complete(poll);
                    } else {
                        promise.fail(asyncResult.cause().getMessage());
                    }
                });

        return promise.future();
    }

    public Future<Void> deletePoll(String pollId) {
        Promise<Void> promise = Promise.promise();

        client.preparedQuery("DELETE FROM poll WHERE id=?")
            .execute(Tuple.of(pollId), asyncResult -> {
                if (asyncResult.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(asyncResult.cause().getMessage());
                }
            });

        return promise.future();
    }
}
