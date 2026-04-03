package com.strategygames.api.controller;

import com.strategygames.api.dto.ApiResponse;
import com.strategygames.api.dto.DecisionExecuteRequest;
import com.strategygames.api.model.DecisionExecution;
import com.strategygames.api.service.DecisionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/decision")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /** GET /decision/available?nationId=xxx */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<String>>> available(@RequestParam String nationId) {
        return ResponseEntity.ok(ApiResponse.ok(decisionService.getAvailable(nationId)));
    }

    /** GET /decision/list — all decisions with metadata */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, DecisionService.DecisionDef>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(decisionService.getAllDecisions()));
    }

    /** POST /decision/execute */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<DecisionExecution>> execute(@Valid @RequestBody DecisionExecuteRequest req) {
        DecisionExecution result = decisionService.execute(
                req.nationId(), req.decisionId(), req.playerUuid(), req.targetNationId());
        return ResponseEntity.ok(ApiResponse.ok("Decision executed", result));
    }
}
