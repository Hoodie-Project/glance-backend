package com.hoodiev.glance.common.util;

import java.util.*;

public class DbscanClusterer {

    private static final int UNVISITED = 0;
    private static final int NOISE = -1;

    public record Point(double lat, double lng) {}
    public record Cluster(double lat, double lng, long count) {}

    public static List<Cluster> cluster(List<Point> points, double epsilon, int minPts) {
        int n = points.size();
        int[] labels = new int[n];
        int clusterId = 0;

        for (int i = 0; i < n; i++) {
            if (labels[i] != UNVISITED) continue;

            List<Integer> neighbors = rangeQuery(points, i, epsilon);
            if (neighbors.size() < minPts) {
                labels[i] = NOISE;
                continue;
            }

            clusterId++;
            labels[i] = clusterId;

            Deque<Integer> seeds = new ArrayDeque<>(neighbors);
            seeds.remove(i);

            while (!seeds.isEmpty()) {
                int q = seeds.poll();
                if (labels[q] == NOISE) labels[q] = clusterId;
                if (labels[q] != UNVISITED) continue;
                labels[q] = clusterId;
                List<Integer> qNeighbors = rangeQuery(points, q, epsilon);
                if (qNeighbors.size() >= minPts) seeds.addAll(qNeighbors);
            }
        }

        Map<Integer, List<Point>> grouped = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (labels[i] > 0) grouped.computeIfAbsent(labels[i], k -> new ArrayList<>()).add(points.get(i));
        }

        return grouped.values().stream()
                .map(pts -> new Cluster(
                        pts.stream().mapToDouble(Point::lat).average().orElse(0),
                        pts.stream().mapToDouble(Point::lng).average().orElse(0),
                        pts.size()))
                .toList();
    }

    private static List<Integer> rangeQuery(List<Point> points, int idx, double epsilon) {
        Point p = points.get(idx);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            double dLat = p.lat() - points.get(i).lat();
            double dLng = p.lng() - points.get(i).lng();
            if (Math.sqrt(dLat * dLat + dLng * dLng) <= epsilon) result.add(i);
        }
        return result;
    }
}
