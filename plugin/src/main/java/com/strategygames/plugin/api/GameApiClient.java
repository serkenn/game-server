package com.strategygames.plugin.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * HTTP client for the game-logic-api Spring Boot service.
 * Uses Java 21's built-in HttpClient (no extra dependencies needed).
 */
public class GameApiClient {

    private final String baseUrl;
    private final HttpClient http;
    private final Gson gson = new Gson();
    private final Logger log;

    public GameApiClient(String baseUrl, Logger log) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.log = log;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // ──────────────── Research ────────────────

    public JsonObject getResearchCandidates(String nationId) {
        return get("/research/candidates?nationId=" + nationId);
    }

    public JsonObject startResearch(String nationId, String technologyId, String playerUuid) {
        JsonObject body = new JsonObject();
        body.addProperty("nationId", nationId);
        body.addProperty("technologyId", technologyId);
        body.addProperty("playerUuid", playerUuid);
        return post("/research/start", body);
    }

    // ──────────────── ICBM ────────────────

    public JsonObject getIcbmStatus(String nationId) {
        return get("/icbm/status?nationId=" + nationId);
    }

    public JsonObject buildIcbm(String nationId) {
        return post("/icbm/build?nationId=" + nationId, null);
    }

    public JsonObject launchIcbm(String attackerNationId, String targetNationId, String playerUuid) {
        JsonObject body = new JsonObject();
        body.addProperty("attackerNationId", attackerNationId);
        body.addProperty("targetNationId", targetNationId);
        body.addProperty("playerUuid", playerUuid);
        return post("/icbm/launch", body);
    }

    // ──────────────── Decision ────────────────

    public JsonObject getAvailableDecisions(String nationId) {
        return get("/decision/available?nationId=" + nationId);
    }

    public JsonObject executeDecision(String nationId, String decisionId,
                                      String playerUuid, String targetNationId) {
        JsonObject body = new JsonObject();
        body.addProperty("nationId", nationId);
        body.addProperty("decisionId", decisionId);
        body.addProperty("playerUuid", playerUuid);
        if (targetNationId != null) body.addProperty("targetNationId", targetNationId);
        return post("/decision/execute", body);
    }

    // ──────────────── Nation ────────────────

    public JsonObject getNationStats(String nationId) {
        return get("/nation/" + nationId + "/stats");
    }

    // ──────────────── HTTP helpers ────────────────

    private JsonObject get(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return JsonParser.parseString(resp.body()).getAsJsonObject();
        } catch (IOException | InterruptedException e) {
            log.warning("API GET failed [" + path + "]: " + e.getMessage());
            return errorResponse(e.getMessage());
        }
    }

    private JsonObject post(String path, JsonObject body) {
        try {
            String json = body != null ? gson.toJson(body) : "{}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return JsonParser.parseString(resp.body()).getAsJsonObject();
        } catch (IOException | InterruptedException e) {
            log.warning("API POST failed [" + path + "]: " + e.getMessage());
            return errorResponse(e.getMessage());
        }
    }

    private JsonObject errorResponse(String msg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", false);
        obj.addProperty("message", msg);
        return obj;
    }
}
