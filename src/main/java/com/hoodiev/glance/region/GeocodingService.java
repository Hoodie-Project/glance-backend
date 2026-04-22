package com.hoodiev.glance.region;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeocodingService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public GeocodingService(
            @Value("${geocoding.naver.client-id:}") String clientId,
            @Value("${geocoding.naver.client-secret:}") String clientSecret) {
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @SuppressWarnings("unchecked")
    public LocationInfo reverseGeocode(double latitude, double longitude) {
        if (clientId == null || clientId.isBlank()) {
            log.warn("Naver API key is not configured, skipping reverse geocoding");
            return null;
        }

        try {
            String coords = longitude + "," + latitude;

            Map<String, Object> response = restClient.get()
                    .uri("https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords={coords}&output=json&orders=addr",
                            coords)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> result = results.get(0);
                    Map<String, Object> code = (Map<String, Object>) result.get("code");
                    Map<String, Object> region = (Map<String, Object>) result.get("region");

                    String legalCode = String.valueOf(code.get("id"));
                    String sido = trim((String) ((Map<?, ?>) region.get("area1")).get("name"));
                    String sigungu = trim((String) ((Map<?, ?>) region.get("area2")).get("name"));
                    String dong = trim((String) ((Map<?, ?>) region.get("area3")).get("name"));

                    if (legalCode.isBlank() || sido.isBlank() || sigungu.isBlank() || dong.isBlank()) {
                        return null;
                    }
                    return new LocationInfo(legalCode, sido, sigungu, dong);
                }
            }
        } catch (Exception e) {
            log.error("Failed to reverse geocode ({}, {}): {}", latitude, longitude, e.getMessage());
        }

        return null;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
