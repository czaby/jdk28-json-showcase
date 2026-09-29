package example;

import jdk.incubator.json.Json;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonString;
import jdk.incubator.json.JsonValue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Expand an IAM policy document: Action/Resource may be a string or an array. */
public final class IamPolicy {
    public static void main(String[] args) throws Exception {
        var policy = load(args);
        System.out.println("Version: " + policy.get("Version").asString());
        for (var statement : members(policy.get("Statement"))) {
            var sid = statement.tryGet("Sid").map(JsonValue::asString).orElse("(no Sid)");
            var effect = statement.get("Effect").asString();
            var actions = strings(statement.get("Action"));
            var resources = strings(statement.get("Resource"));
            for (var action : actions) {
                var star = action.equals("*") || action.endsWith(":*") ? "  <-- wildcard" : "";
                System.out.printf("%s  %s  %s  %s%s%n", effect, sid, action, resources, star);
            }
        }
    }

    static List<JsonValue> members(JsonValue value) {
        return switch (value) {
            case JsonArray a -> a.asList();
            case JsonObject o -> List.of(o);
            default -> List.of();
        };
    }

    static List<String> strings(JsonValue value) {
        return switch (value) {
            case JsonString s -> List.of(s.asString());
            case JsonArray a -> a.asList().stream().map(JsonValue::asString).toList();
            default -> List.of();
        };
    }

    static JsonValue load(String[] args) throws Exception {
        if (args.length > 0) {
            return Json.parse(Files.readString(Path.of(args[0])));
        }
        try (var in = IamPolicy.class.getResourceAsStream("/iam-policy.json")) {
            return Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
