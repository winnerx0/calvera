package com.winnerx0.calvera.events.internal;

import com.winnerx0.calvera.common.ApiResponse;
import com.winnerx0.calvera.events.CiEventService;
import com.winnerx0.calvera.events.CiEventView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
class CiEventController {

    private final CiEventService ciEventService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CiEventView>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(ciEventService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CiEventView>> getById(@PathVariable Long id) {
        return ciEventService.findById(id)
                .map(view -> ResponseEntity.ok(ApiResponse.ok(view)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Event not found")));
    }
}
