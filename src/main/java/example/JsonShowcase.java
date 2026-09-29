package example;

import jdk.incubator.json.Json;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonBoolean;
import jdk.incubator.json.JsonNull;
import jdk.incubator.json.JsonNumber;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonParseException;
import jdk.incubator.json.JsonString;
import jdk.incubator.json.JsonValue;
import jdk.incubator.json.JsonValueException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Walks through JEP 540 / {@code jdk.incubator.json} as it exists in JDK 28.
 *
 * <p>Compile and run with {@code --add-modules jdk.incubator.json}.
 */
public final class JsonShowcase {

    public static void main(String[] args) throws Exception {
        new JsonShowcase().run();
    }

    void run() throws Exception {
        banner("1. Parse RFC 8259 text");
        parseSection();

        banner("2. Navigate with get / tryGet / tryValue");
        navigateSection();

        banner("3. Convert to Java types");
        convertSection();

        banner("4. Pattern-match the sealed JsonValue hierarchy");
        patternMatchSection();

        banner("5. Build a document with factories");
        constructSection();

        banner("6. Generate compact and pretty JSON");
        generateSection();

        banner("7. Fail-fast errors");
        errorSection();
    }

    void parseSection() throws IOException {
        JsonValue fromString = Json.parse(sampleJson());
        JsonValue fromChars = Json.parse(sampleJson().toCharArray());

        out("parse(String)  -> " + typeName(fromString));
        out("parse(char[])  -> " + typeName(fromChars));
        out("root members   -> " + fromString.asMap().keySet());
    }

    void navigateSection() throws IOException {
        JsonValue weather = Json.parse(sampleJson());

        String city = weather.get("location").get("city").asString();
        int firstTemp = weather.get("periods").get(0).get("temperature").asInt();
        out("city           -> " + city);
        out("first temp     -> " + firstTemp);

        Optional<JsonValue> missing = weather.tryGet("alerts");
        out("tryGet alerts  -> " + missing);

        Optional<JsonValue> owner = weather.get("owner").tryValue();
        out("owner tryValue -> " + owner + "  (empty because the member is JSON null)");
    }

    void convertSection() throws IOException {
        JsonValue weather = Json.parse(sampleJson());

        String service = weather.get("service").asString();
        int version = weather.get("version").asInt();
        long versionLong = weather.get("version").asLong();
        double lat = weather.get("location").get("lat").asDouble();
        boolean active = weather.get("active").asBoolean();
        Map<String, JsonValue> location = weather.get("location").asMap();
        List<JsonValue> periods = weather.get("periods").asList();

        double average = periods.stream()
                .mapToInt(p -> p.get("temperature").asInt())
                .average()
                .orElseThrow();

        out("service        -> " + service);
        out("version        -> " + version + " / " + versionLong);
        out("lat            -> " + lat);
        out("active         -> " + active);
        out("location keys  -> " + location.keySet());
        out("period count   -> " + periods.size());
        out("avg temp       -> " + average);
    }

    void patternMatchSection() {
        JsonValue mixed = Json.parse("""
                { \"tid\": \"42\", \"count\": 7, \"ok\": true, \"note\": null }
                """);

        out("tid   -> " + asLongFlexible(mixed.get("tid")));
        out("count -> " + asLongFlexible(mixed.get("count")));
        out("ok    -> " + describe(mixed.get("ok")));
        out("note  -> " + describe(mixed.get("note")));
    }

    void constructSection() {
        JsonObject built = ordered(
                "providers", JsonArray.of(List.of(
                        JsonString.of("SUN"),
                        JsonString.of("SunRsaSign"),
                        JsonString.of("SunEC"))),
                "version", JsonNumber.of(1),
                "enabled", JsonBoolean.of(true),
                "owner", JsonNull.of(),
                "ratio", JsonNumber.of("0.25"));

        out("factory tree   -> " + built);
        out("providers[1]   -> " + built.get("providers").get(1).asString());
        out("ratio asDouble -> " + built.get("ratio").asDouble());
    }

    void generateSection() {
        JsonValue value = Json.parse("{ \"service\" : \"web_server\", \"id\" : 3 }");
        out("toString() compact:");
        out(value.toString());
        out("toDisplayString() pretty:");
        out(Json.toDisplayString(value, "  "));
    }

    void errorSection() {
        try {
            Json.parse("{ \"a\": 1, \"a\": 2 }");
        } catch (JsonParseException e) {
            out("duplicate name -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            Json.parse("{ \"a\": 1, }");
        } catch (JsonParseException e) {
            out("trailing comma -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        JsonValue weather = Json.parse(sampleJson());
        try {
            weather.get("missing");
        } catch (JsonValueException e) {
            out("missing member -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            weather.get("version").asString();
        } catch (JsonValueException e) {
            out("wrong type     -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    static long asLongFlexible(JsonValue value) {
        return switch (value) {
            case JsonNumber n -> n.asLong();
            case JsonString s -> Long.parseLong(s.asString());
            default -> throw new JsonValueException("tid/count is neither number nor string");
        };
    }

    static String describe(JsonValue value) {
        return switch (value) {
            case JsonObject o -> "object " + o.asMap().keySet();
            case JsonArray a -> "array[" + a.asList().size() + "]";
            case JsonString s -> "string " + s.asString();
            case JsonNumber n -> "number " + n;
            case JsonBoolean b -> "boolean " + b.asBoolean();
            case JsonNull _ -> "null";
        };
    }

    static String typeName(JsonValue value) {
        return value.getClass().getInterfaces()[0].getSimpleName();
    }

    static JsonObject ordered(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("expected key/value pairs");
        }
        Map<String, JsonValue> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], (JsonValue) keyValues[i + 1]);
        }
        return JsonObject.of(map);
    }

    static String sampleJson() {
        try (InputStream in = JsonShowcase.class.getResourceAsStream("/sample.json")) {
            if (in == null) {
                throw new IllegalStateException("missing classpath resource /sample.json");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void banner(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    static void out(String line) {
        System.out.println(line);
    }
}
