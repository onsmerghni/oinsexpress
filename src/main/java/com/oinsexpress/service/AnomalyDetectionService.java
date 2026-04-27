package com.oinsexpress.service;

import com.oinsexpress.dto.TrackingDtos.PositionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Service de détection d'anomalies via le modèle XGBoost (Python).
 * Pour des raisons de performance, on utilise un seuil simple en plus
 * pour éviter d'invoquer Python à chaque requête.
 */
@Service
@Slf4j
public class AnomalyDetectionService {

    @Value("${oinsexpress.ml.python-script}")
    private String pythonScript;

    // Seuils empiriques (ajustables après entraînement)
    private static final double ACC_THRESHOLD = 15.0; // m/s²
    private static final double GYR_THRESHOLD = 250.0; // deg/s

    public boolean isAnomaly(PositionRequest req) {
        // Filtre rapide par seuil
        double accMagnitude = Math.sqrt(
            sq(req.getAccX()) + sq(req.getAccY()) + sq(req.getAccZ())
        );
        double gyrMagnitude = Math.sqrt(
            sq(req.getGyrX()) + sq(req.getGyrY()) + sq(req.getGyrZ())
        );

        if (accMagnitude > ACC_THRESHOLD || gyrMagnitude > GYR_THRESHOLD) {
            log.info("Anomalie détectée par seuil — acc={}, gyr={}", accMagnitude, gyrMagnitude);
            return true;
        }

        // Si activé, appel au modèle XGBoost
        if (Files.exists(Paths.get(pythonScript))) {
            return invokeXGBoost(req);
        }

        return false;
    }

    private boolean invokeXGBoost(PositionRequest req) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "python3", pythonScript,
                String.valueOf(req.getAccX()),
                String.valueOf(req.getAccY()),
                String.valueOf(req.getAccZ()),
                String.valueOf(req.getGyrX()),
                String.valueOf(req.getGyrY()),
                String.valueOf(req.getGyrZ())
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                return "ANOMALY".equalsIgnoreCase(line);
            }
        } catch (Exception e) {
            log.error("Erreur appel XGBoost : {}", e.getMessage());
            return false;
        }
    }

    private double sq(Double v) {
        return v == null ? 0 : v * v;
    }
}
