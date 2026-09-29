package example;

import jdk.incubator.json.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Fetch Jira ABC-123 and print labels plus the last comment. */
public final class JiraTicket {
    public static void main(String[] args) throws Exception {
        var key = args.length > 0 ? args[0] : "ABC-123";
        var json = Json.parse(HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(System.getenv("JIRA_BASE_URL")
                                + "/rest/api/2/issue/" + key + "?fields=labels,comment"))
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                                (System.getenv("JIRA_EMAIL") + ":" + System.getenv("JIRA_API_TOKEN"))
                                        .getBytes(StandardCharsets.UTF_8)))
                        .header("Accept", "application/json")
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()).body());

        var fields = json.get("fields");
        System.out.println("labels: " + fields.get("labels").asList().stream()
                .map(v -> v.asString()).toList());
        var comments = fields.get("comment").get("comments").asList();
        System.out.println("last comment: " +
                (comments.isEmpty() ? "(none)" : comments.getLast().get("body").asString()));
    }
}
