package example;

import jdk.incubator.json.Json;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonParseException;
import jdk.incubator.json.JsonValue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * {@code terraform show -json} is one RFC 8259 document — {@code Json.parse} once.
 * {@code terraform plan -json} is JSONL — {@code Json.parse} each line.
 * The incubator API is not a stream parser; it does not read concatenated values.
 */
public final class TerraformPlan {
    public static void main(String[] args) throws Exception {
        var text = loadText(args);
        try {
            summarizeShow(Json.parse(text));
        } catch (JsonParseException e) {
            var counts = new LinkedHashMap<String, Integer>();
            text.lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty())
                    .map(Json::parse)
                    .forEach(event -> handleUiEvent(event, counts));
            System.out.println("totals: " + counts);
        }
    }

    static void summarizeShow(JsonValue plan) {
        var counts = new LinkedHashMap<String, Integer>();
        for (var rc : plan.get("resource_changes").asList()) {
            var actions = rc.get("change").get("actions").asList()
                    .stream().map(JsonValue::asString).toList();
            var kind = kind(actions);
            counts.merge(kind, 1, Integer::sum);
            System.out.printf("%s  %s  %s%n",
                    kind, rc.get("address").asString(), afterKeys(rc.get("change")));
        }
        System.out.println("totals: " + counts);
    }

    static void handleUiEvent(JsonValue event, LinkedHashMap<String, Integer> counts) {
        switch (event.get("type").asString()) {
            case "planned_change" -> {
                var change = event.get("change");
                var action = change.get("action").asString();
                counts.merge(action, 1, Integer::sum);
                System.out.printf("%s  %s%n", action, change.get("resource").get("addr").asString());
            }
            case "change_summary" -> {
                var c = event.get("changes");
                System.out.printf("summary  add=%d change=%d remove=%d%n",
                        c.get("add").asInt(), c.get("change").asInt(), c.get("remove").asInt());
            }
            default -> {
            }
        }
    }

    static String kind(List<String> actions) {
        if (actions.equals(List.of("create"))) {
            return "create";
        }
        if (actions.equals(List.of("update"))) {
            return "update";
        }
        if (actions.equals(List.of("delete"))) {
            return "delete";
        }
        if (actions.contains("create") && actions.contains("delete")) {
            return "replace";
        }
        return String.join("+", actions);
    }

    static String afterKeys(JsonValue change) {
        return switch (change.get("after")) {
            case JsonObject after -> {
                var known = after.asMap().keySet();
                var unknown = switch (change.get("after_unknown")) {
                    case JsonObject u -> u.asMap().keySet();
                    default -> java.util.Set.<String>of();
                };
                yield "after=" + known + " unknown=" + unknown;
            }
            default -> "after=null";
        };
    }

    static String loadText(String[] args) throws Exception {
        if (args.length > 0) {
            return Files.readString(Path.of(args[0]));
        }
        try (var in = TerraformPlan.class.getResourceAsStream("/terraform-plan.json")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
