package com.autodonut.ai;

import com.autodonut.config.AutoDonutConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal client for talking to an OpenAI-compatible "chat completions" API.
 * Works with OpenAI itself, or any compatible proxy/self-hosted endpoint
 * (just change aiBaseUrl in config/autodonut.json).
 *
 * The API key never leaves this machine except in the single HTTPS request
 * made directly to aiBaseUrl.
 */
public class AiClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public static CompletableFuture<String> ask(String systemPrompt, String userPrompt) {
        AutoDonutConfig cfg = AutoDonutConfig.get();
        if (cfg.aiApiKey == null || cfg.aiApiKey.isBlank()) {
            return CompletableFuture.completedFuture(
                    "[AutoDonut AI] No API key set. Use /autodonut ai setkey <key> first.");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.aiModel);
        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", systemPrompt);
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userPrompt);
        messages.add(user);

        body.add("messages", messages);
        body.addProperty("temperature", 0.6);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cfg.aiBaseUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + cfg.aiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(AiClient::extractContent)
                .exceptionally(ex -> "[AutoDonut AI] Request failed: " + ex.getMessage());
    }

    private static String extractContent(HttpResponse<String> response) {
        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("error")) {
                return "[AutoDonut AI] API error: " + json.getAsJsonObject("error").get("message").getAsString();
            }
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "[AutoDonut AI] No response returned (HTTP " + response.statusCode() + ").";
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            return message.get("content").getAsString().trim();
        } catch (Exception e) {
            return "[AutoDonut AI] Couldn't parse response (HTTP " + response.statusCode() + "): "
                    + trim(response.body());
        }
    }

    private static String trim(String s) {
        if (s == null) return "";
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
