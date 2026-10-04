package vn.ai.farmer;

/**
 * Trạng thái = vị trí 4 đối tượng. false = bờ TRÁI (bờ xuất phát), true = bờ PHẢI (đích).
 * Thuyền luôn đi cùng nông dân nên không cần lưu riêng.
 */
public record State(boolean f, boolean w, boolean g, boolean c) {
    public static final String[] NAMES = {"Nông dân", "Sói", "Dê", "Bắp cải"};
    private static final String[] SHORT = {"N", "S", "D", "B"};

    public static State start() { return new State(false, false, false, false); }

    public boolean at(int i) {
        return switch (i) { case 0 -> f; case 1 -> w; case 2 -> g; default -> c; };
    }

    /** item = 0: đi một mình; 1=Sói, 2=Dê, 3=Bắp cải. Chưa kiểm tra hợp lệ. */
    public State move(int item) {
        boolean nf = !f;
        return new State(nf, item == 1 ? nf : w, item == 2 ? nf : g, item == 3 ? nf : c);
    }

    /** Trả về lý do trạng thái SAI (có thứ bị ăn), hoặc null nếu an toàn. */
    public String violation() {
        if (w == g && f != w) return "Sói ở cùng Dê mà không có nông dân → Sói ăn Dê";
        if (g == c && f != g) return "Dê ở cùng Bắp cải mà không có nông dân → Dê ăn Bắp cải";
        return null;
    }

    public boolean isGoal() { return f && w && g && c; }

    /** Số vật (Sói, Dê, Bắp cải) còn ở bờ trái. */
    public int leftItems() { return (w ? 0 : 1) + (g ? 0 : 1) + (c ? 0 : 1); }

    /**
     * Heuristic h(n) = cận dưới của số lần qua sông còn lại.
     * Mỗi chuyến chở tối đa 1 vật; giữa 2 chuyến chở vật liên tiếp phải có 1 chuyến quay về.
     *  - Nông dân bờ trái, k vật ở trái : k chuyến sang + (k-1) chuyến về = 2k-1 (tối thiểu 1)
     *  - Nông dân bờ phải, k vật ở trái : 1 chuyến về + (2k-1) = 2k
     */
    public int h() {
        if (isGoal()) return 0;
        int k = leftItems();
        return f ? 2 * k : Math.max(1, 2 * k - 1);
    }

    public String hExplain() {
        if (isGoal()) return "Đã đủ 4 đối tượng ở bờ phải → còn 0 chuyến";
        int k = leftItems();
        if (f) return "Nông dân ở bờ phải, còn " + k + " vật ở bờ trái: 1 chuyến quay về + " + k
                + " chuyến chở sang + " + (k - 1) + " chuyến quay về = " + (2 * k);
        if (k == 0) return "Nông dân ở bờ trái một mình, các vật đã sang hết: cần tối thiểu 1 chuyến";
        return "Nông dân ở bờ trái, còn " + k + " vật: " + k + " chuyến chở sang + " + (k - 1)
                + " chuyến quay về = " + (2 * k - 1);
    }

    public String label() {
        StringBuilder l = new StringBuilder(), r = new StringBuilder();
        for (int i = 0; i < 4; i++) (at(i) ? r : l).append(SHORT[i]).append(' ');
        return "Trái[" + l.toString().trim() + "] | Phải[" + r.toString().trim() + "]";
    }
}
