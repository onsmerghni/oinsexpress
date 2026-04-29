package com.oinsexpress.controller;

import com.oinsexpress.dto.SensorDTO;
import com.oinsexpress.ml.MLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * SensorController — Reçoit les données IMU de l'ESP32
 *
 * Flux :
 *   ESP32 → POST /api/sensor/data → MLService → Flask IA → prédiction
 *
 * PFA 2026 — ISTIC
 * Mrabet Islem & Merghni Ons
 */
@RestController
@RequestMapping("/api/sensor")
@CrossOrigin(origins = "*")
public class SensorController {

    @Autowired
    private MLService mlService;

    /**
     * POST /api/sensor/data
     *
     * Reçoit les données brutes de l'ESP32,
     * appelle Flask IA et retourne la prédiction.
     *
     * Body JSON :
     * {
     *   "ax": 0.012, "ay": -0.003, "az": 1.001,
     *   "gx": 0.5,  "gy": -0.3,  "gz": 0.1,
     *   "livreurId": "LIV-001"
     * }
     */
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> receiveData(@RequestBody SensorDTO dto) {

        Map<String, Object> result = new HashMap<>();

        // ── Appel Flask IA ──
        String prediction = mlService.predict(
            dto.getAx(), dto.getAy(), dto.getAz(),
            dto.getGx(), dto.getGy(), dto.getGz()
        );

        result.put("livreurId",  dto.getLivreurId());
        result.put("prediction", prediction);

        // ── Réponse selon prédiction ──
        if ("AGGRESSIVE".equals(prediction)) {
            result.put("status",  "ALERTE");
            result.put("message", "Conduite agressive détectée !");
            System.out.println("[ALERTE] Livreur " + dto.getLivreurId()
                + " → conduite AGGRESSIVE");
        } else {
            result.put("status",  "OK");
            result.put("message", "Conduite normale");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/sensor/health
     *
     * Vérifie que le service Flask IA est disponible
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        boolean iaUp = mlService.isHealthy();

        result.put("spring_boot", "UP");
        result.put("flask_ia",    iaUp ? "UP" : "DOWN");
        result.put("status",      iaUp ? "OK" : "DEGRADED");

        return ResponseEntity.ok(result);
    }
}
