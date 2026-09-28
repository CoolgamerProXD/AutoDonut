package com.autodonut.client.safety;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/** Optional: posts a plain text alert to a Discord webhook URL you configure. Fully optional, off unless a URL is set. */
public final class DiscordWebhook {

    // One shared client for the lifetime of the game process. Deliberately never
    // closed: closing it would tear down the executor that in-flight alerts are
    // still using, and the JVM reclaims it on exit anyway.
    @SuppressWarnings("resource")
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    private DiscordWebhook() {}

    public static void send(String webhookUrl, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;
        String json = "{\"content\":\"" + escape(message) + "\"}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            CompletableFuture<HttpResponse<Void>> future = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            future.exceptionally(ex -> null); // best-effort - never let a failed webhook affect gameplay
        } catch (RuntimeException ignored) {
            // Invalid URL or similar - silently skip, this is a nice-to-have, not critical.
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
