package com.example;
import java.util.*;
import java.util.stream.Collectors;
public class App {
    public static void main(String[] args) {
        // 1. Khởi tạo danh sách kề (các đường đi giữa các thành phố)
        Map<String, List<String>> graph = Map.of(
            "A", List.of("B", "C"),
            "B", List.of("D"),
            "C", List.of("D", "E"),
            "D", List.of("F"),
            "E", List.of("F"),
            "F", List.<String>of()
        );
        Map<String, Integer> h = Map.of(
            "A", 10,
            "B", 8,
            "C", 6,
            "D", 4,
            "E", 2,
            "F", 0
        );
        List<String> queue = new ArrayList<>(); // Hàng đợi 
        Set<String> visited = new HashSet<>();  // Tập các đỉnh đã đưa vào hàng đợi
        Map<String, String> parent = new HashMap<>(); // Lưu cha của đỉnh để truy vết
        String startNode = "A";
        String targetNode = "F";
        queue.add(startNode);
        visited.add(startNode);
        java.util.function.Function<String, String> formatNode = 
            node -> node + "(" + h.get(node) + ")";
        System.out.println("Kết quả chạy thuật toán Best-First Search từ " + startNode + " đến " + targetNode + ":\n");
        System.out.printf("%-4s | %-18s | %-3s | %-15s | %-22s | %-20s%n", 
                "STT", "L", "U", "V", "father(v)", "Người cha thực sự");
        System.out.println("-".repeat(95));
        int stt = 1;
        boolean found = false;
        while (!queue.isEmpty()) {
            String lStr = queue.stream().map(formatNode).collect(Collectors.joining(", "));
            String u = queue.remove(0);
            if (u.equals(targetNode)) {
                String lNewStr = queue.stream().map(formatNode).collect(Collectors.joining(", "));
                if (lNewStr.isEmpty()) lNewStr = "Ø";
                System.out.printf("%-4d | %-18s | %-3s | %-15s | %-22s | %-20s%n", 
                        stt, lStr, u, "Ø", lNewStr, "Đã tìm thấy đích");
                found = true;
                break;
            }
            List<String> neighbors = graph.getOrDefault(u, List.<String>of());
            List<String> vList = new ArrayList<>();
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, u);
                    queue.add(neighbor);
                    vList.add(neighbor);
                }
            }
            queue.sort((n1, n2) -> {
                int cmp = Integer.compare(h.get(n1), h.get(n2));
                return (cmp != 0) ? cmp : n1.compareTo(n2);
            });
            String vStr = vList.isEmpty() ? "Ø" : vList.stream().map(formatNode).collect(Collectors.joining(", "));
            String fatherVStr = queue.isEmpty() ? "Ø" : queue.stream().map(formatNode).collect(Collectors.joining(", "));
            String parentNote = vList.isEmpty() ? "-" : vList.stream()
                .map(v -> "Cha " + v + "=" + u)
                .collect(Collectors.joining(", "));
            System.out.printf("%-4d | %-18s | %-3s | %-15s | %-22s | %-20s%n", 
                    stt, lStr, u, vStr, fatherVStr, parentNote);   
            stt++;
        }
        System.out.println("-".repeat(95));
        if (found) {
            List<String> path = new ArrayList<>();
            String current = targetNode;
            while (current != null) {
                path.add(current);
                current = parent.get(current);
            }
            Collections.reverse(path);
            System.out.println("\nĐường đi ngắn nhất (Best-First Search): " + String.join(" -> ", path));
            System.out.println("Tổng chi phí cạnh (nếu mỗi cạnh = 1): " + (path.size() - 1));
        }
    }
}
