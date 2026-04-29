package com.oinsexpress.ml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * MLService — Appel vers Flask IA (XGBoost)
 * PFA 2026 — ISTIC | Mrabet Islem & Merghni Ons
 */
@Service
public class MLService {

    @Value("${ML_API_URL}")
    private String mlApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Envoie les données IMU au Flask IA → retourne "NORMAL" ou "AGGRESSIVE"
     */
    public String predict(double ax, double ay, double az,
                          double gx, double gy, double gz) {
        try {
            // ── Corps JSON ──
            Map<String, Double> body = new HashMap<>();
            body.put("ax", ax);
            body.put("ay", ay);
            body.put("az", az);
            body.put("gx", gx);
            body.put("gy", gy);
            body.put("gz", gz);

            // ── Headers ──
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Double>> request = new HttpEntity<>(body, headers);

            // ── Appel Flask → réponse String brute ──
            ResponseEntity<String> response = restTemplate.postForEntity(
                mlApiUrl + "/predict",
                request,
                String.class
            );

            // ── Parser le JSON manuellement ──
            if (response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("prediction")) {
                    return json.get("prediction").asText();
                }
            }

            return "UNKNOWN";

        } catch (Exception e) {
            System.err.println("[MLService] Erreur appel IA : " + e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Vérifie que Flask IA est disponible
     */
    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                mlApiUrl + "/health",
                String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("[MLService] Health check failed : " + e.getMessage());
            return false;
        }
    }
}