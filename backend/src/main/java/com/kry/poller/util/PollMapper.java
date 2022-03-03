package com.kry.poller.util;

import com.kry.poller.model.Poll;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class PollMapper {

    public static JsonObject mapToJsonObject(Poll poll) {
        return JsonObject.mapFrom(poll);
    }

    public static Poll mapToPoll(JsonObject poll) {
        return poll.mapTo(Poll.class);
    }

    public static JsonArray mapToJsonArray(List<Poll> polls) {
        return polls.stream()
            .map(PollMapper::mapToJsonObject)
            .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::add));
    }

    public static List<Poll> mapToList(RowSet<Row> polls) {
        List<Poll> mappedPolls = new ArrayList<>();
        polls.forEach(poll -> mappedPolls.add(mapToPoll(poll.toJson())));

        return mappedPolls;
    }
}
