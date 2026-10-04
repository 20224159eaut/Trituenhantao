# Nông dân – Sói – Dê – Bắp cải (BFS vs A*)
Yêu cầu: JDK 26, Maven 3.9+. Chạy: `mvn spring-boot:run` rồi mở http://localhost:8080
Nếu lỗi "Unsupported class file major version 70": đổi `<java.version>` trong pom.xml thành 25.

Tab "4. Vai trò của heuristic": cách xây dựng h(n) bằng nới lỏng bài toán, kiểm chứng admissible/consistent so với h* thật, và thí nghiệm A* với h₀, h₁, h₂, h*, h₃ (API `/api/solve`, khoá `heur`).
