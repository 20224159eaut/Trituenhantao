package com.thanhtest;

import java.util.*;

public class App {
    public static void main(String[] args) {
        // 1. Khai báo đồ thị rõ ràng kiểu Map<String, List<String>> để tránh lỗi Type mismatch
        Map<String, List<String>> graph = Map.ofEntries(
            Map.entry("A", List.of("C", "D", "F")),
            Map.entry("C", List.of("B", "E")),
            Map.entry("D", List.of("G")),
            // Sử dụng List.<String>of() để báo cho Java biết đây là List rỗng chứa String
            Map.entry("F", List.<String>of()),
            Map.entry("B", List.<String>of()),
            Map.entry("E", List.<String>of()),
            Map.entry("G", List.of("H", "I")),
            Map.entry("H", List.of("K", "M")),
            Map.entry("I", List.<String>of()),
            Map.entry("K", List.<String>of()),
            Map.entry("M", List.<String>of())
        );

        // 2. Khởi tạo các cấu trúc dữ liệu cho BFS
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>(); // Lưu cha của v để truy vết

        String startNode = "A";
        String targetNode = "I";

        // Bắt đầu từ đỉnh A
        queue.add(startNode);
        visited.add(startNode);

        // In tiêu đề bảng
        System.out.println("Kết quả chạy thuật toán BFS từ " + startNode + " đến " + targetNode + ":\n");
        System.out.printf("%-5s | %-15s | %-3s | %-10s | %-15s%n", "STT", "L", "U", "V", "father(v)");
        System.out.println("-".repeat(60));

        int stt = 1;
        boolean found = false;

        // 3. Vòng lặp BFS
        while (!queue.isEmpty()) {
            // L: Trạng thái hàng đợi trước khi lấy
            String lStr = String.join(", ", queue); 
            
            // U: Lấy đỉnh ra khỏi đầu hàng đợi
            String u = queue.poll(); 

            // Nếu u là đích đến -> In dòng cuối và Dừng
            if (u.equals(targetNode)) {
                System.out.printf("%-5d | %-15s | %-3s | %-10s | %-15s%n", stt, lStr, u, "Ø", String.join(", ", queue));
                found = true;
                break;
            }

            // Lấy các đỉnh kề của u (Khai báo rõ kiểu List<String>)
            List<String> neighbors = graph.getOrDefault(u, List.<String>of());
            List<String> vList = new ArrayList<>(); // Lưu các đỉnh V (đỉnh kề chưa đánh dấu)

            // Duyệt qua các đỉnh kề
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, u); // Ghi nhận u là cha của neighbor
                    queue.add(neighbor);     // Đưa vào hàng đợi
                    vList.add(neighbor);
                }
            }

            // Chuẩn bị dữ liệu để in ra console cho giống bảng
            String vStr = vList.isEmpty() ? "Ø" : String.join(", ", vList);
            String fatherVStr = queue.isEmpty() ? "Ø" : String.join(", ", queue);

            System.out.printf("%-5d | %-15s | %-3s | %-10s | %-15s%n", stt, lStr, u, vStr, fatherVStr);
            stt++;
        }

        System.out.println("-".repeat(60));

        // 4. Truy vết đường đi nếu tìm thấy
        if (found) {
            List<String> path = new ArrayList<>();
            String current = targetNode;
            
            while (current != null) {
                path.add(current);
                current = parent.get(current);
            }
            
            Collections.reverse(path);
            System.out.println("\nĐã tìm thấy đích! Đường đi ngắn nhất: " + String.join(" -> ", path));
        } else {
            System.out.println("\nKhông tìm thấy đường đi từ " + startNode + " đến " + targetNode);
        }
    }
}