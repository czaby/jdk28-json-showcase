package example;

import jdk.incubator.json.Json;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/** Read username/password from an AWS Secrets Manager JSON secret. */
public final class SecretLogin {
    public static void main(String[] args) {
        try (var sm = SecretsManagerClient.create()) {
            var json = Json.parse(sm.getSecretValue(b -> b.secretId(args[0])).secretString());
            var username = json.get("username").asString();
            var password = json.get("password").asString();
            System.out.printf("%s / %s%n", username, "*".repeat(password.length()));
        }
    }
}
