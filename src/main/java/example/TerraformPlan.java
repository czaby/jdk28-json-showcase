package example;

import jdk.incubator.json.Json;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonValue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Summarize Terraform JSON.
 *
 * <p>{@code terraform show -json plan.bin} is one object with {@code resource_changes}.
 * {@code terraform plan -json} is a UI event stream: one JSON value after another
 * (JSONL or pretty-printed concatenated objects). {@code Json.parse} rejects that
 * with "Additional value(s) were found after the JSON Value".
 */
public final class TerraformPlan {
    public static void main(String[] args) throws Exception {
        var documents = parseDocuments(loadText(args));
        if (documents.size() == 1 && documents.getFirst().tryGet("resource_changes").isPresent()) {
            summarizeShowJson(documents.getFirst());
        } else {
            summarizePlanUi(documents);
        }
    }

    static void summarizeShowJson(JsonValue plan) {
        var counts = new LinkedHashMap<String, Integer>();
        for (var rc : plan.get("resource_changes").asList()) {
            var actions = rc.get("change").get("actions").asList()
                    .stream().map(JsonValue::asString).toList();
            var kind = kind(actions);
            counts.merge(kind, 1, Integer::sum);
            System.out.printf("%s  %s  %s%n", kind, rc.get("address").asString(), afterKeys(rc.get("change")));
        }
        System.out.println("totals: " + counts);
    }

    static void summarizePlanUi(List<JsonValue> events) {
        var counts = new LinkedHashMap<String, Integer>();
        for (var event : events) {
            var type = event.tryGet("type").map(JsonValue::asString).orElse("");
            switch (type) {
                case "planned_change" -> {
                    var change = event.get("change");
                    var action = change.get("action").asString();
                    var addr = change.get("resource").get("addr").asString();
                    counts.merge(action, 1, Integer::sum);
                    System.out.printf("%s  %s%n", action, addr);
                }
                case "change_summary" -> {
                    var changes = event.get("changes");
                    System.out.printf("summary  add=%d change=%d remove=%d%n",
                            changes.get("add").asInt(),
                            changes.get("change").asInt(),
                            changes.get("remove").asInt());
                }
                default -> {
                }
            }
        }
        System.out.println("totals: " + counts);
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

    static List<JsonValue> parseDocuments(String text) {
        var documents = new ArrayList<JsonValue>();
        for (var chunk : splitJsonValues(text)) {
            documents.add(Json.parse(chunk));
        }
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("no JSON values in input");
        }
        return documents;
    }

    /** Split concatenated / JSONL values by brace depth so pretty-printed events work. */
    static List<String> splitJsonValues(String text) {
        var parts = new ArrayList<String>();
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        int start = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '{' || c == '[') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}' || c == ']') {
                depth--;
                if (depth == 0 && start >= 0) {
                    parts.add(text.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return parts;
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
