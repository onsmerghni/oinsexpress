package com.oinsexpress.ml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * MLService — Appel vers Flask IA (XGBoost)
 * URL : https://oinsexpress-ia.onrender.com/predict
 *
 * PFA 2026 — ISTIC
 * Mrabet Islem & Merghni Ons
 */
@Service
public class MLService {

    @Value("${ML_API_URL}")
    private String mlApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envoie les données IMU au service Flask IA et retourne la prédiction.
     *
     * @return "NORMAL" ou "AGGRESSIVE" ou "UNKNOWN" si erreur
     */
    public String predict(double ax, double ay, double az,
                          double gx, double gy, double gz) {
        try {
            // ── Construire le corps JSON ──
            Map<String, Double> body = new HashMap<>();
            body.put("ax", ax);
            body.put("ay", ay);
            body.put("az", az);
            body.put("gx", gx);
            body.put("gy", gy);
            body.put("gz", gz);

            // ── Appeler Flask /predict ──
            ResponseEntity<Map> response = restTemplate.postForEntity(
                mlApiUrl + "/predict",
                body,
                Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("prediction")) {
                return (String) response.getBody().get("prediction");
            }

            return "UNKNOWN";

        } catch (Exception e) {
            System.err.println("[MLService] Erreur appel IA : " + e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Retourne true si le service IA est disponible
     */
    public boolean isHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                mlApiUrl + "/health", Map.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
