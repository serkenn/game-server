package com.strategygames.api.controller;

import com.strategygames.api.dto.ApiResponse;
import com.strategygames.api.service.NationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/nation")
public class NationController {

    private final NationService nationService;

    public NationController(NationService nationService) {
        this.nationService = nationService;
    }

    /** GET /nation/{id}/stats */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(nationService.getStats(id)));
    }
}
