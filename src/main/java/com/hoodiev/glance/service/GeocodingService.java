package com.hoodiev.glance.service;

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
    public String reverseGeocode(double latitude, double longitude) {
        if (clientId == null || clientId.isBlank()) {
            log.warn("Naver API key is not configured, skipping reverse geocoding");
            return null;
        }

        try {
            String coords = longitude + "," + latitude;

            Map<String, Object> response = restClient.get()
                    .uri("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords={coords}&output=json&orders=roadaddr,addr",
                            coords)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> region = (Map<String, Object>) results.get(0).get("region");
                    String area1 = ((Map<String, String>) region.get("area1")).get("name");
                    String area2 = ((Map<String, String>) region.get("area2")).get("name");
                    String area3 = ((Map<String, String>) region.get("area3")).get("name");
                    return area1 + " " + area2 + " " + area3;
                }
            }
        } catch (Exception e) {
            log.error("Failed to reverse geocode ({}, {}): {}", latitude, longitude, e.getMessage());
        }

        return null;
    }
}
