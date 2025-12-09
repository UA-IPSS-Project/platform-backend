package pt.florinhas.marcacoes.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from backend!");
    }
    
    @PostMapping("/echo")
    public Map<String, Object> echo(@RequestBody Map<String, Object> data) {
        return Map.of(
            "received", data,
            "timestamp", System.currentTimeMillis()
        );
    }
}
