package example;

import jdk.incubator.json.Json;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonValue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/** Flatten Atlassian Document Format (Jira API v3 comment bodies) to text. */
public final class JiraAdf {
    public static void main(String[] args) throws Exception {
        var json = load(args);
        var comments = json.tryGet("fields")
                .flatMap(v -> v.tryGet("comment"))
                .flatMap(v -> v.tryGet("comments"))
                .map(v -> v.asList())
                .orElse(null);
        if (comments == null) {
            System.out.println(flatten(json));
            return;
        }
        if (comments.isEmpty()) {
            System.out.println("(no comments)");
            return;
        }
        var last = comments.getLast();
        System.out.println("author: " + last.get("author").get("displayName").asString());
        System.out.println(flatten(last.get("body")));
    }

    static String flatten(JsonValue value) {
        var out = new StringBuilder();
        walk(value, out);
        return out.toString().strip();
    }

    static void walk(JsonValue value, StringBuilder out) {
        switch (value) {
            case JsonObject o -> {
                var type = o.tryGet("type").map(JsonValue::asString).orElse("");
                switch (type) {
                    case "text" -> o.tryGet("text").ifPresent(t -> out.append(t.asString()));
                    case "hardBreak" -> out.append('\n');
                    case "paragraph", "heading", "listItem", "blockquote" -> {
                        o.tryGet("content").ifPresent(c -> walk(c, out));
                        out.append('\n');
                    }
                    default -> o.tryGet("content").ifPresent(c -> walk(c, out));
                }
            }
            case JsonArray a -> a.asList().forEach(v -> walk(v, out));
            default -> {
            }
        }
    }

    static JsonValue load(String[] args) throws Exception {
        if (args.length > 0 && Files.isRegularFile(Path.of(args[0]))) {
            return Json.parse(Files.readString(Path.of(args[0])));
        }
        if (args.length > 0 && System.getenv("JIRA_BASE_URL") != null) {
            var key = args[0];
            var auth = Base64.getEncoder().encodeToString(
                    (System.getenv("JIRA_EMAIL") + ":" + System.getenv("JIRA_API_TOKEN"))
                            .getBytes(StandardCharsets.UTF_8));
            var body = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(System.getenv("JIRA_BASE_URL")
                                    + "/rest/api/3/issue/" + key + "?fields=comment"))
                            .header("Authorization", "Basic " + auth)
                            .header("Accept", "application/json")
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString()).body();
            return Json.parse(body);
        }
        try (var in = JiraAdf.class.getResourceAsStream("/adf-comment.json")) {
            return Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
