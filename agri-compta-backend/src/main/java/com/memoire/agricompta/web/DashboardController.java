package com.memoire.agricompta.web;

import com.memoire.agricompta.service.DashboardService;
import com.memoire.agricompta.web.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @GetMapping
    public DashboardResponse dashboard() {
        return service.getDashboard();
    }
}
