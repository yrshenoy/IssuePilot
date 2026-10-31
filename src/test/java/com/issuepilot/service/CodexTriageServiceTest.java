package com.issuepilot.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodexTriageServiceTest {
    @Test
    void commandUsesReadOnlySandboxAndNumericIssueId() {
        var service = new CodexTriageService("codex", ".");
        try {
            List<String> command = service.commandFor(2408, Path.of("report.txt"));
            assertThat(command).containsSubsequence("exec", "--ephemeral", "--sandbox", "read-only");
            assertThat(command).containsSubsequence("--output-last-message", "report.txt");
            assertThat(command.get(command.size() - 1)).isEqualTo("$issue-pilot 2408");
            assertThat(command).doesNotContain("--yolo", "danger-full-access", "workspace-write");
        } finally {
            service.shutdown();
        }
    }

    @Test
    void initiallyHasNoActiveJobAndNoStoredReport() {
        var service = new CodexTriageService("codex", ".");
        try {
            assertThat(service.activeIssueId()).isNull();
            assertThat(service.status(2408).state()).isEqualTo("NOT_STARTED");
        } finally {
            service.shutdown();
        }
    }

    @Test
    void rejectsInvalidIssueId() {
        var service = new CodexTriageService("codex", ".");
        try {
            assertThatThrownBy(() -> service.commandFor(0, Path.of("report.txt")))
                    .isInstanceOf(IllegalArgumentException.class);
            var result = service.start(-1);
            assertThat(result.state()).isEqualTo("FAILED");
            assertThat(service.status(-1).state()).isEqualTo("NOT_STARTED");
        } finally {
            service.shutdown();
        }
    }
}
