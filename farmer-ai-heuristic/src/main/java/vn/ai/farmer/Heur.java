package vn.ai.farmer;

/** Các heuristic dùng để so sánh vai trò của h(n) trong A*. */
public enum Heur {
    ZERO("h₀(n) = 0", "Không dùng thông tin gì về đích (A* thoái hoá thành tìm kiếm chi phí đều)"),
    COUNT("h₁(n) = số vật còn ở bờ trái", "Admissible nhưng yếu: bỏ qua các chuyến quay về của nông dân"),
    MAIN("h₂(n) = số chuyến tối thiểu (heuristic của bài)", "Đếm cả chuyến chở sang lẫn chuyến quay về"),
    PERFECT("h*(n) = chi phí thật (heuristic lý tưởng)", "Giới hạn tốt nhất có thể đạt: A* đi thẳng theo đường tối ưu"),
    OVER("h₃(n) = 3·h₂(n)", "Cố ý ước lượng quá cao → không còn admissible");

    public final String title, note;
    Heur(String title, String note) { this.title = title; this.note = note; }

    public int of(State s) {
        if (s.isGoal()) return 0;
        return switch (this) {
            case ZERO -> 0;
            case COUNT -> s.leftItems();
            case MAIN -> s.h();
            case PERFECT -> HeuristicLab.trueCost().get(s);
            case OVER -> 3 * s.h();
        };
    }
}
