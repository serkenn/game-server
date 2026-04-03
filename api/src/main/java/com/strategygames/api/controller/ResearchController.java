package com.strategygames.api.controller;

import com.strategygames.api.dto.ApiResponse;
import com.strategygames.api.dto.ResearchStartRequest;
import com.strategygames.api.model.ResearchProgress;
import com.strategygames.api.model.Technology;
import com.strategygames.api.service.ResearchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/research")
public class ResearchController {

    private final ResearchService researchService;

    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    /** GET /research/candidates?nationId=xxx — Stellaris-style candidate list */
    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<Technology>>> getCandidates(@RequestParam String nationId) {
        return ResponseEntity.ok(ApiResponse.ok(researchService.getCandidates(nationId)));
    }

    /** POST /research/start */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ResearchProgress>> start(@Valid @RequestBody ResearchStartRequest req) {
        ResearchProgress progress = researchService.startResearch(req.nationId(), req.technologyId(), req.playerUuid());
        return ResponseEntity.ok(ApiResponse.ok("Research started", progress));
    }
}
