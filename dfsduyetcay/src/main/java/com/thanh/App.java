package com.thanh;

import java.util.*;

public class App {
    public static void main(String[] args) {
        // 1. Khởi tạo đồ thị theo đúng hình ảnh image_e44d60.png
        Map<String, List<String>> graph = Map.ofEntries(
            Map.entry("A", List.of("B", "C", "D")),
            Map.entry("B", List.of("E", "F")),
            Map.entry("C", List.of("G", "H")),
            Map.entry("D", List.of("I", "J")),
            Map.entry("E", List.of("K", "L")),
            Map.entry("F", List.of("L", "M")), // F nối tới L và M
            Map.entry("G", List.of("N")),
            Map.entry("H", List.of("O", "P")),
            Map.entry("I", List.of("P", "Q")),
            Map.entry("J", List.of("N")),
            Map.entry("K", List.of("S")),
            Map.entry("L", List.of("T")),
            Map.entry("P", List.of("U")),
            
            // Các đỉnh lá không có nhánh con
            Map.entry("M", List.<String>of()),
            Map.entry("N", List.<String>of()),
            Map.entry("O", List.<String>of()),
            Map.entry("Q", List.<String>of()),
            Map.entry("S", List.<String>of()),
            Map.entry("T", List.<String>of()),
            Map.entry("U", List.<String>of())
        );

        // 2. Khởi tạo cấu trúc dữ liệu cho DFS
        // Dùng LinkedList như một Stack (chèn và lấy ra ở đầu danh sách)
        LinkedList<String> stack = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>();

        String startNode = "A";
        String targetNode = "P";

        // Bắt đầu từ đỉnh A
        stack.add(startNode);
        visited.add(startNode);

        // In tiêu đề bảng
        System.out.println("Kết quả chạy thuật toán DFS từ " + startNode + " đến " + targetNode + ":\n");
        System.out.printf("%-4s | %-15s | %-3s | %-10s | %-15s%n", "STT", "L", "U", "V", "father(v)");
        System.out.println("-".repeat(60));

        int stt = 1;
        boolean found = false;

        // 3. Vòng lặp duyệt DFS
        while (!stack.isEmpty()) {
            // L: Trạng thái ngăn xếp trước khi lấy phần tử
            String lStr = String.join(", ", stack);
            
            // U: Lấy đỉnh ra khỏi đầu ngăn xếp (Last-In-First-Out behavior)
            String u = stack.removeFirst();

            // Nếu u là đỉnh đích P -> kết thúc
            if (u.equals(targetNode)) {
                System.out.printf("%-4d | %-15s | %-3s | %-10s | %-15s%n", stt, lStr, u, "Ø", String.join(", ", stack));
                found = true;
                break;
            }

            // Lấy các đỉnh kề của u
            List<String> neighbors = graph.getOrDefault(u, List.<String>of());
            List<String> vList = new ArrayList<>();

            // Duyệt qua các đỉnh kề chưa thăm
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, u);
                    vList.add(neighbor);
                }
            }

            // Đưa V vào ĐẦU ngăn xếp để ưu tiên duyệt sâu nhánh này
            // Cách này chèn cả cụm vList vào đầu mà vẫn giữ đúng thứ tự từ trái sang phải
            stack.addAll(0, vList);

            // Xử lý chuỗi in ra giao diện
            String vStr = vList.isEmpty() ? "Ø" : String.join(", ", vList);
            String fatherVStr = stack.isEmpty() ? "Ø" : String.join(", ", stack);

            System.out.printf("%-4d | %-15s | %-3s | %-10s | %-15s%n", stt, lStr, u, vStr, fatherVStr);
            stt++;
        }

        System.out.println("-".repeat(60));

        // 4. Truy vết đường đi
        if (found) {
            List<String> path = new ArrayList<>();
            String current = targetNode;
            
            while (current != null) {
                path.add(current);
                current = parent.get(current);
            }
            
            Collections.reverse(path);
            System.out.println("\nĐã tìm thấy đích! Đường đi DFS: " + String.join(" -> ", path));
        }
    }
}