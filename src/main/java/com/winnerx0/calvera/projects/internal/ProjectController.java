package com.winnerx0.calvera.projects.internal;

import com.winnerx0.calvera.common.ApiResponse;
import com.winnerx0.calvera.projects.CreateProjectRequest;
import com.winnerx0.calvera.projects.ProjectService;
import com.winnerx0.calvera.projects.ProjectView;
import com.winnerx0.calvera.projects.UpdateProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectView>>> getAll(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findAll(userId(authentication))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectView>> getById(@PathVariable Long id, Authentication authentication) {
        return projectService.findById(id, userId(authentication))
                .map(view -> ResponseEntity.ok(ApiResponse.ok(view)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Project not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectView>> create(@RequestBody CreateProjectRequest request, Authentication authentication) {
        ProjectView view = projectService.create(request, userId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(view));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectView>> update(@PathVariable Long id, @RequestBody UpdateProjectRequest request, Authentication authentication) {
        return projectService.update(id, request, userId(authentication))
                .map(view -> ResponseEntity.ok(ApiResponse.ok(view)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Project not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication authentication) {
        return projectService.delete(id, userId(authentication))
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).<ApiResponse<Void>>build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Project not found"));
    }

    private Long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
