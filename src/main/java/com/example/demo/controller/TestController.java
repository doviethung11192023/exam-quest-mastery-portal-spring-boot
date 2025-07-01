
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// @RestController
// @RequestMapping("/test")
// public class TestController {

//     @GetMapping("/cors")
//     public Map<String, String> testCors() {
//         Map<String, String> response = new HashMap<>();
//         response.put("message", "CORS is working!");
//         return response;
//     }
// }
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from backend");
        System.err.println("Hello endpoint called");
        return response;
    }
}