package example;

import jdk.incubator.json.Json;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonValue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Summarize `terraform show -json` resource_changes. */
public final class TerraformPlan {
    public static void main(String[] args) throws Exception {
        var plan = load(args);
        var counts = new LinkedHashMap<String, Integer>();
        for (var rc : plan.get("resource_changes").asList()) {
            var actions = rc.get("change").get("actions").asList()
                    .stream().map(JsonValue::asString).toList();
            var kind = kind(actions);
            counts.merge(kind, 1, Integer::sum);
            var change = rc.get("change");
            System.out.printf("%s  %s  %s%n", kind, rc.get("address").asString(), afterKeys(change));
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

    static JsonValue load(String[] args) throws Exception {
        if (args.length > 0) {
            return Json.parse(Files.readString(Path.of(args[0])));
        }
        try (var in = TerraformPlan.class.getResourceAsStream("/terraform-plan.json")) {
            return Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
