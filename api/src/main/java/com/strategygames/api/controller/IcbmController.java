package com.strategygames.api.controller;

import com.strategygames.api.dto.ApiResponse;
import com.strategygames.api.dto.IcbmLaunchRequest;
import com.strategygames.api.model.IcbmSilo;
import com.strategygames.api.service.IcbmService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/icbm")
public class IcbmController {

    private final IcbmService icbmService;

    public IcbmController(IcbmService icbmService) {
        this.icbmService = icbmService;
    }

    /** GET /icbm/status?nationId=xxx */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<IcbmSilo>> getStatus(@RequestParam String nationId) {
        return ResponseEntity.ok(ApiResponse.ok(icbmService.getStatus(nationId)));
    }

    /** POST /icbm/build — start silo construction */
    @PostMapping("/build")
    public ResponseEntity<ApiResponse<IcbmSilo>> build(@RequestParam String nationId) {
        return ResponseEntity.ok(ApiResponse.ok("Construction started", icbmService.startConstruction(nationId)));
    }

    /** POST /icbm/launch */
    @PostMapping("/launch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> launch(@Valid @RequestBody IcbmLaunchRequest req) {
        Map<String, Object> result = icbmService.launch(req.attackerNationId(), req.targetNationId(), req.playerUuid());
        return ResponseEntity.ok(ApiResponse.ok("Launch executed", result));
    }
}
