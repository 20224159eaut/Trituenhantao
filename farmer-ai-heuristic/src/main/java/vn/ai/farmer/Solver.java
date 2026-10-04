package vn.ai.farmer;

import java.util.*;

public class Solver {

    public record Candidate(String action, String state, String status, String reason,
                            Integer g, Integer h, Integer f) {}

    public record Step(int no, String current, Integer g, Integer h, Integer f, String explain,
                       List<Candidate> candidates, List<String> queue, List<String> visited) {}

    public record Result(String name, boolean found, List<Step> steps, List<String> path, int cost,
                         int expanded, int generated, int invalid, int maxFrontier) {}

    private record Node(State s, Node parent, String action, int g, int h, int seq) {
        int f() { return g + h; }
    }

    private static final String[] ACT = {"Nông dân đi một mình", "Nông dân chở Sói",
            "Nông dân chở Dê", "Nông dân chở Bắp cải"};

    private static String action(State s, int i) {
        return ACT[i] + (s.f() ? " về bờ trái" : " sang bờ phải");
    }

    private static List<String> path(Node goal) {
        LinkedList<String> p = new LinkedList<>();
        for (Node n = goal; n != null; n = n.parent)
            p.addFirst((n.parent == null ? "Bắt đầu" : n.action) + "  →  " + n.s.label());
        return p;
    }

    // ------------------------------------------------------------------ BFS
    public static Result bfs() {
        Deque<Node> queue = new ArrayDeque<>();
        Set<State> visited = new LinkedHashSet<>();
        List<Step> steps = new ArrayList<>();
        Node root = new Node(State.start(), null, null, 0, 0, 0);
        queue.add(root);
        visited.add(root.s);
        int no = 0, expanded = 0, generated = 0, invalid = 0, maxF = 1;
        Node found = null;

        while (!queue.isEmpty()) {
            Node n = queue.poll();                       // FIFO: lấy phần tử ĐẦU hàng đợi
            no++;
            if (n.s.isGoal()) {
                found = n;
                steps.add(new Step(no, n.s.label(), n.g, null, null,
                        "Lấy khỏi đầu hàng đợi trạng thái ĐÍCH (cả 4 ở bờ phải) → BFS dừng. "
                                + "Vì BFS duyệt theo từng tầng nên đường đi tìm được có ít bước nhất.",
                        List.of(), snapQ(queue), snapV(visited)));
                break;
            }
            expanded++;
            List<Candidate> cands = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String act = action(n.s, i);
                if (i > 0 && n.s.at(i) != n.s.f()) {
                    cands.add(new Candidate(act, "—", "KHÔNG THỂ",
                            State.NAMES[i] + " đang ở bờ đối diện với thuyền", null, null, null));
                    continue;
                }
                State ns = n.s.move(i);
                generated++;
                String v = ns.violation();
                if (v != null) {
                    invalid++;
                    cands.add(new Candidate(act, ns.label(), "SAI", v + " → loại, không cho vào hàng đợi",
                            null, null, null));
                } else if (visited.contains(ns)) {
                    cands.add(new Candidate(act, ns.label(), "ĐÃ THĂM",
                            "Trạng thái đã có trong tập visited → bỏ qua để không lặp vòng", null, null, null));
                } else {
                    visited.add(ns);
                    queue.add(new Node(ns, n, act, n.g + 1, 0, 0));   // thêm vào CUỐI hàng đợi
                    cands.add(new Candidate(act, ns.label(), "THÊM VÀO HÀNG ĐỢI",
                            "Hợp lệ và chưa thăm → thêm vào cuối hàng đợi (độ sâu " + (n.g + 1) + ")",
                            null, null, null));
                }
            }
            maxF = Math.max(maxF, queue.size());
            steps.add(new Step(no, n.s.label(), n.g, null, null,
                    "Lấy phần tử ĐẦU hàng đợi (độ sâu " + n.g + ") ra để mở rộng. Thử cả 4 hành động của nông dân.",
                    cands, snapQ(queue), snapV(visited)));
        }
        return new Result("BFS", found != null, steps, found == null ? List.of() : path(found),
                found == null ? -1 : found.g, expanded, generated, invalid, maxF);
    }

    private static List<String> snapQ(Collection<Node> q) {
        return q.stream().map(x -> x.s.label() + "  (độ sâu " + x.g + ")").toList();
    }

    private static List<String> snapV(Collection<State> v) {
        return v.stream().map(State::label).toList();
    }

    // ------------------------------------------------------------------ A*
    public static Result astar() { return astar(Heur.MAIN); }

    public static Result astar(Heur hz) {
        Comparator<Node> cmp = Comparator.comparingInt(Node::f).thenComparingInt(Node::h).thenComparingInt(Node::seq);
        PriorityQueue<Node> open = new PriorityQueue<>(cmp);
        Map<State, Integer> best = new HashMap<>();
        List<Step> steps = new ArrayList<>();
        int seq = 0;
        State st = State.start();
        Node root = new Node(st, null, null, 0, hz.of(st), seq++);
        open.add(root);
        best.put(st, 0);
        Set<State> closed = new LinkedHashSet<>();
        int no = 0, expanded = 0, generated = 0, invalid = 0, maxF = 1;
        Node found = null;

        while (!open.isEmpty()) {
            Node n = open.poll();                        // lấy nút có f nhỏ nhất
            if (n.g > best.get(n.s)) continue;           // bản cũ, đã có đường tốt hơn
            no++;
            String head = "g(n) = " + n.g + ": đã đi " + n.g + " lần qua sông từ trạng thái đầu. "
                    + "h(n) = " + n.h + ": " + ex(hz, n.s) + ". "
                    + "f(n) = g(n) + h(n) = " + n.g + " + " + n.h + " = " + n.f() + ". ";
            if (n.s.isGoal()) {
                found = n;
                steps.add(new Step(no, n.s.label(), n.g, n.h, n.f(),
                        head + "Nút có f nhỏ nhất trong OPEN chính là ĐÍCH → A* dừng. "
                                + "Vì h không bao giờ ước lượng quá cao (admissible) nên đường này tối ưu.",
                        List.of(), snapO(open, cmp), snapV(closed)));
                break;
            }
            expanded++;
            closed.add(n.s);
            List<Candidate> cands = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String act = action(n.s, i);
                if (i > 0 && n.s.at(i) != n.s.f()) {
                    cands.add(new Candidate(act, "—", "KHÔNG THỂ",
                            State.NAMES[i] + " đang ở bờ đối diện với thuyền", null, null, null));
                    continue;
                }
                State ns = n.s.move(i);
                generated++;
                String v = ns.violation();
                if (v != null) {
                    invalid++;
                    cands.add(new Candidate(act, ns.label(), "SAI", v + " → loại, không tính h/f",
                            null, null, null));
                    continue;
                }
                int g2 = n.g + 1, h2 = hz.of(ns);
                Integer old = best.get(ns);
                if (old != null && old <= g2) {
                    cands.add(new Candidate(act, ns.label(), "BỎ QUA",
                            "Đã có đường tới trạng thái này với g=" + old + " ≤ " + g2 + " → không tốt hơn",
                            g2, h2, g2 + h2));
                } else {
                    best.put(ns, g2);
                    open.add(new Node(ns, n, act, g2, h2, seq++));
                    cands.add(new Candidate(act, ns.label(), "THÊM VÀO OPEN",
                            "g = " + n.g + " + 1 = " + g2 + ";  h = " + h2 + " (" + ex(hz, ns) + ");  f = "
                                    + g2 + " + " + h2 + " = " + (g2 + h2), g2, h2, g2 + h2));
                }
            }
            maxF = Math.max(maxF, open.size());
            steps.add(new Step(no, n.s.label(), n.g, n.h, n.f(),
                    head + "Đây là nút có f nhỏ nhất trong OPEN (hoà thì chọn h nhỏ hơn) nên được mở rộng.",
                    cands, snapO(open, cmp), snapV(closed)));
        }
        return new Result(hz == Heur.MAIN ? "A*" : "A* với " + hz.title, found != null, steps, found == null ? List.of() : path(found),
                found == null ? -1 : found.g, expanded, generated, invalid, maxF);
    }

    private static String ex(Heur hz, State s) {
        return hz == Heur.MAIN ? s.hExplain() : hz.title + " = " + hz.of(s);
    }

    private static List<String> snapO(Collection<Node> open, Comparator<Node> cmp) {
        return open.stream().sorted(cmp)
                .map(x -> x.s.label() + "  (g=" + x.g + ", h=" + x.h + ", f=" + x.f() + ")").toList();
    }

    // ------------------------------------------------------------------ Không gian trạng thái
    public static List<Map<String, Object>> stateSpace() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int b = 0; b < 16; b++) {
            State s = new State((b & 8) != 0, (b & 4) != 0, (b & 2) != 0, (b & 1) != 0);
            String v = s.violation();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", "" + (s.f() ? 1 : 0) + (s.w() ? 1 : 0) + (s.g() ? 1 : 0) + (s.c() ? 1 : 0));
            m.put("label", s.label());
            m.put("valid", v == null);
            m.put("reason", v == null ? "An toàn" : v);
            m.put("h", v == null ? s.h() : null);
            out.add(m);
        }
        return out;
    }
}
