package vn.ai.farmer;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class FarmerApplication {
    public static void main(String[] args) { SpringApplication.run(FarmerApplication.class, args); }

    @GetMapping("/api/solve")
    public Map<String, Object> solve() {
        return Map.of("bfs", Solver.bfs(), "astar", Solver.astar(), "states", Solver.stateSpace(),
                "heur", HeuristicLab.analyze());
    }
}
