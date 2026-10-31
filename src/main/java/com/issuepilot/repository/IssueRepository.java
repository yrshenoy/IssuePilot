
package com.issuepilot.repository;

import com.issuepilot.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository
        extends JpaRepository<Issue, Long> {
}
