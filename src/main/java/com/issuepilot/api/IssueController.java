
package com.issuepilot.api;

import com.issuepilot.model.Issue;
import com.issuepilot.repository.IssueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueRepository repository;

    public IssueController(IssueRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Issue> getAllIssues() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(
            @PathVariable Long id) {

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}
