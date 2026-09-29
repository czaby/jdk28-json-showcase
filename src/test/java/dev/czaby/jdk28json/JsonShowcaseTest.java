package dev.czaby.jdk28json;

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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonShowcaseTest {

    private static final String SAMPLE = JsonShowcase.sampleJson();

    @Test
    void parseStringAndCharArray() {
        JsonValue fromString = Json.parse(SAMPLE);
        JsonValue fromChars = Json.parse(SAMPLE.toCharArray());

        assertInstanceOf(JsonObject.class, fromString);
        assertEquals(fromString.toString(), fromChars.toString());
    }

    @Test
    void navigateRequiredAndOptionalMembers() {
        JsonValue weather = Json.parse(SAMPLE);

        assertEquals("Berlin", weather.get("location").get("city").asString());
        assertEquals(14, weather.get("periods").get(0).get("temperature").asInt());
        assertTrue(weather.tryGet("alerts").isEmpty());
        assertTrue(weather.get("owner").tryValue().isEmpty());
    }

    @Test
    void convertEachJsonKind() {
        JsonValue weather = Json.parse(SAMPLE);

        assertEquals("weather", weather.get("service").asString());
        assertEquals(1, weather.get("version").asInt());
        assertEquals(1L, weather.get("version").asLong());
        assertEquals(52.52, weather.get("location").get("lat").asDouble());
        assertTrue(weather.get("active").asBoolean());
        assertEquals(2, weather.get("location").asMap().size());
        assertEquals(3, weather.get("periods").asList().size());
    }

    @Test
    void patternMatchNumberOrString() {
        JsonValue mixed = Json.parse("""
                { "tid": "42", "count": 7 }
                """);

        assertEquals(42L, JsonShowcase.asLongFlexible(mixed.get("tid")));
        assertEquals(7L, JsonShowcase.asLongFlexible(mixed.get("count")));
    }

    @Test
    void factoriesBuildARoundTrippableTree() {
        JsonObject built = JsonObject.of(Map.of(
                "name", JsonString.of("SUN"),
                "version", JsonNumber.of(1),
                "on", JsonBoolean.of(true),
                "owner", JsonNull.of(),
                "tags", JsonArray.of(List.of(JsonString.of("core")))));

        JsonValue parsed = Json.parse(built.toString());
        assertEquals("SUN", parsed.get("name").asString());
        assertEquals(1, parsed.get("version").asInt());
        assertTrue(parsed.get("on").asBoolean());
        assertInstanceOf(JsonNull.class, parsed.get("owner"));
        assertEquals("core", parsed.get("tags").get(0).asString());
    }

    @Test
    void numberFactoryAcceptsExactText() {
        JsonNumber precise = JsonNumber.of("0.25");
        assertEquals(0.25, precise.asDouble());
        assertThrows(JsonValueException.class, precise::asInt);
    }

    @Test
    void prettyPrintUsesRequestedIndent() {
        JsonValue value = Json.parse("{ \"id\" : 3 }");
        String pretty = Json.toDisplayString(value, "  ");

        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("  \"id\""));
        assertEquals(value.toString(), Json.parse(pretty).toString());
    }

    @Test
    void parseRejectsDuplicatesAndTrailingCommas() {
        assertThrows(JsonParseException.class, () -> Json.parse("{ \"a\": 1, \"a\": 2 }"));
        assertThrows(JsonParseException.class, () -> Json.parse("{ \"a\": 1, }"));
    }

    @Test
    void accessRejectsMissingMembersAndWrongTypes() {
        JsonValue weather = Json.parse(SAMPLE);

        JsonValueException missing = assertThrows(JsonValueException.class, () -> weather.get("nope"));
        assertFalse(missing.getMessage().isBlank());

        JsonValueException wrongType = assertThrows(JsonValueException.class,
                () -> weather.get("version").asString());
        assertFalse(wrongType.getMessage().isBlank());
    }

    @Test
    void tryGetOnNonObjectFails() {
        JsonValue number = Json.parse("1");
        assertThrows(JsonValueException.class, () -> number.tryGet("x"));
    }

    @Test
    void describeCoversEverySubtype() {
        assertTrue(JsonShowcase.describe(Json.parse("{}")).startsWith("object"));
        assertTrue(JsonShowcase.describe(Json.parse("[]")).startsWith("array"));
        assertEquals("string hi", JsonShowcase.describe(Json.parse("\"hi\"")));
        assertTrue(JsonShowcase.describe(Json.parse("3")).startsWith("number"));
        assertEquals("boolean true", JsonShowcase.describe(Json.parse("true")));
        assertEquals("null", JsonShowcase.describe(Json.parse("null")));
    }
}
