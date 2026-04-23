package com.hoodiev.glance.region

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class GeocodingService(
    @Value("\${geocoding.naver.client-id:}") private val clientId: String,
    @Value("\${geocoding.naver.client-secret:}") private val clientSecret: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    @Suppress("UNCHECKED_CAST")
    fun reverseGeocode(latitude: Double, longitude: Double): LocationInfo? {
        if (clientId.isBlank()) {
            log.warn("Naver API key is not configured, skipping reverse geocoding")
            return null
        }

        return try {
            val response = restClient.get()
                .uri(
                    "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords={coords}&output=json&orders=addr",
                    "$longitude,$latitude"
                )
                .header("X-NCP-APIGW-API-KEY-ID", clientId)
                .header("X-NCP-APIGW-API-KEY", clientSecret)
                .retrieve()
                .body(object : ParameterizedTypeReference<Map<String, Any>>() {})

            val result = (response?.get("results") as? List<Map<String, Any>>)
                ?.firstOrNull() ?: return null

            val code = result["code"] as? Map<*, *> ?: return null
            val region = result["region"] as? Map<*, *> ?: return null

            val legalCode = code["id"]?.toString() ?: return null
            val sido = (region["area1"] as? Map<*, *>)?.get("name") as? String ?: return null
            val sigungu = (region["area2"] as? Map<*, *>)?.get("name") as? String ?: return null
            val dong = (region["area3"] as? Map<*, *>)?.get("name") as? String ?: return null

            if (legalCode.isBlank() || sido.isBlank() || sigungu.isBlank() || dong.isBlank()) {
                return null
            }

            LocationInfo(legalCode, sido.trim(), sigungu.trim(), dong.trim())
        } catch (e: Exception) {
            log.error("Failed to reverse geocode ({}, {}): {}", latitude, longitude, e.message)
            null
        }
    }
}
