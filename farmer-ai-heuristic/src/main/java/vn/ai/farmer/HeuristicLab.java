package vn.ai.farmer;

import java.util.*;
import vn.ai.farmer.Solver.Result;

/** Phân tích vai trò của heuristic: h* thật, kiểm tra admissible / consistent, so sánh các heuristic. */
public class HeuristicLab {

    private static String code(State s) {
        return "" + (s.f() ? 1 : 0) + (s.w() ? 1 : 0) + (s.g() ? 1 : 0) + (s.c() ? 1 : 0);
    }

    /** Đổi nhãn "Trái[..] | Phải[..]" thành mã 4 bit N S D B. */
    private static String codeOfLabel(String label) {
        String right = label.substring(label.indexOf("Phải[") + 5, label.lastIndexOf(']'));
        StringBuilder sb = new StringBuilder();
        for (String ch : new String[]{"N", "S", "D", "B"}) sb.append(Arrays.asList(right.split(" ")).contains(ch) ? '1' : '0');
        return sb.toString();
    }

    private static List<State> validStates() {
        List<State> out = new ArrayList<>();
        for (int b = 0; b < 16; b++) {
            State s = new State((b & 8) != 0, (b & 4) != 0, (b & 2) != 0, (b & 1) != 0);
            if (s.violation() == null) out.add(s);
        }
        return out;
    }

    private static List<State> neighbors(State s) {
        List<State> out = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i > 0 && s.at(i) != s.f()) continue;
            State t = s.move(i);
            if (t.violation() == null) out.add(t);
        }
        return out;
    }

    /** h*(n): chi phí thật tới đích, tính bằng BFS ngược từ đích (đồ thị vô hướng, mỗi cạnh chi phí 1). */
    private static Map<State, Integer> CACHE;

    public static synchronized Map<State, Integer> trueCost() {
        if (CACHE != null) return CACHE;
        Map<State, Integer> d = new HashMap<>();
        Deque<State> q = new ArrayDeque<>();
        State goal = new State(true, true, true, true);
        d.put(goal, 0); q.add(goal);
        while (!q.isEmpty()) {
            State s = q.poll();
            for (State t : neighbors(s)) if (d.putIfAbsent(t, d.get(s) + 1) == null) q.add(t);
        }
        return CACHE = d;
    }

    public static Map<String, Object> analyze() {
        Map<State, Integer> star = trueCost();
        List<State> valid = validStates();
        Heur[] hs = Heur.values();

        List<Map<String, Object>> table = new ArrayList<>();
        for (State s : valid) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", code(s)); m.put("label", s.label()); m.put("hstar", star.get(s));
            for (Heur h : hs) m.put(h.name(), h.of(s));
            table.add(m);
        }

        List<Map<String, Object>> props = new ArrayList<>();
        for (Heur h : hs) {
            List<String> notAdm = new ArrayList<>(), notCons = new ArrayList<>();
            int edges = 0;
            for (State s : valid) {
                if (h.of(s) > star.get(s)) notAdm.add(code(s) + ": h=" + h.of(s) + " > h*=" + star.get(s));
                for (State t : neighbors(s)) {
                    edges++;
                    if (h.of(s) > 1 + h.of(t))
                        notCons.add(code(s) + "→" + code(t) + ": h(n)=" + h.of(s) + " > 1 + h(n')=" + (1 + h.of(t)));
                }
            }
            Result r = run(h);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.name()); m.put("title", h.title); m.put("note", h.note);
            m.put("admissible", notAdm.isEmpty()); m.put("violAdm", notAdm);
            m.put("consistent", notCons.isEmpty()); m.put("violCons", notCons); m.put("edges", edges);
            m.put("expanded", r.expanded()); m.put("generated", r.generated()); m.put("cost", r.cost());
            m.put("optimal", r.cost() == star.get(State.start()));
            m.put("order", r.steps().stream().map(s -> codeOfLabel(s.current()) + (s.f() == null ? "" : " (f=" + s.f() + ")")).toList());
            props.add(m);
        }
        Result bfs = Solver.bfs();
        return Map.of("table", table, "props", props, "optimum", star.get(State.start()),
                "bfsExpanded", bfs.expanded(), "bfsGenerated", bfs.generated());
    }

    private static Result run(Heur h) { return Solver.astar(h); }

}
