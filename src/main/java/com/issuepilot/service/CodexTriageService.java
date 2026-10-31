package com.issuepilot.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Single-user, local-only Codex CLI bridge. Reports and status survive Vaadin
 * navigation, but are stored only in process memory (not in H2).
 */
@Service
public class CodexTriageService {
    private static final Duration TIMEOUT = Duration.ofMinutes(3);
    private static final int MAX_REPORT_CHARS = 120_000;
    private static final int MAX_DIAGNOSTIC_CHARS = 2500;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "issuepilot-codex");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<Long, TriageStatus> results = new ConcurrentHashMap<>();
    private final String executable;
    private final Path workspace;
    private Long activeIssueId;

    public CodexTriageService(
            @Value("${issuepilot.codex.executable:}") String configuredExecutable,
            @Value("${issuepilot.codex.workspace:}") String configuredWorkspace) {
        this.executable = configuredExecutable.isBlank()
                ? (System.getProperty("os.name").toLowerCase().contains("win") ? "codex.exe" : "codex")
                : configuredExecutable;
        this.workspace = configuredWorkspace.isBlank()
                ? Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
                : Path.of(configuredWorkspace).toAbsolutePath().normalize();
    }

    /** Start exactly one job, returning its state immediately. */
    public synchronized TriageStatus start(long issueId) {
        if (issueId <= 0) return failed(issueId, "Invalid issue ID.");
        if (activeIssueId != null) {
            if (activeIssueId == issueId) return results.get(issueId);
            return failed(issueId, "Issue #" + activeIssueId
                    + " is currently being analyzed. Its report will be available on that issue.");
        }
        TriageStatus state = new TriageStatus(issueId, "RUNNING",
                "Running Codex analysis for issue #" + issueId + "…", Instant.now());
        activeIssueId = issueId;
        results.put(issueId, state);
        try {
            executor.execute(() -> {
                TriageResult result;
                try {
                    result = execute(issueId);
                } catch (RuntimeException e) {
                    result = TriageResult.failure("Unexpected Codex integration error: "
                            + e.getClass().getSimpleName());
                }
                synchronized (CodexTriageService.this) {
                    results.put(issueId, new TriageStatus(issueId,
                            result.success() ? "COMPLETED" : "FAILED",
                            result.message(), Instant.now()));
                    activeIssueId = null;
                }
            });
        } catch (RuntimeException e) {
            activeIssueId = null;
            TriageStatus stateFailed = failed(issueId, "Unable to schedule Codex analysis.");
            results.put(issueId, stateFailed);
            return stateFailed;
        }
        return state;
    }

    public synchronized TriageStatus status(long issueId) {
        return results.getOrDefault(issueId, new TriageStatus(issueId, "NOT_STARTED",
                "Advisory analysis · Human review required", null));
    }

    public synchronized Long activeIssueId() {
        return activeIssueId;
    }

    private static TriageStatus failed(long issueId, String message) {
        return new TriageStatus(issueId, "FAILED", message, Instant.now());
    }

    private TriageResult execute(long issueId) {
        Path reportFile = null;
        Path logFile = null;
        Process process = null;
        try {
            if (!Files.isRegularFile(workspace.resolve(".agents/skills/issue-pilot/SKILL.md"))) {
                return TriageResult.failure("IssuePilot skill not found in " + workspace
                        + ". Configure ISSUEPILOT_CODEX_WORKSPACE.");
            }
            reportFile = Files.createTempFile("issuepilot-report-", ".txt");
            logFile = Files.createTempFile("issuepilot-codex-", ".log");
            ProcessBuilder builder = new ProcessBuilder(commandFor(issueId, reportFile));
            builder.directory(workspace.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(logFile.toFile());
            process = builder.start();

            if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                terminateProcessTree(process);
                return TriageResult.failure("Codex timed out after 3 minutes for issue #" + issueId
                        + ". Diagnostic output: " + diagnostic(logFile));
            }
            if (process.exitValue() != 0) {
                return TriageResult.failure("Codex exited with code " + process.exitValue()
                        + ". Diagnostic output: " + diagnostic(logFile));
            }
            String report = Files.readString(reportFile, StandardCharsets.UTF_8).trim();
            if (report.isBlank()) {
                return TriageResult.failure("Codex returned an empty report. Diagnostic output: "
                        + diagnostic(logFile));
            }
            if (report.length() > MAX_REPORT_CHARS) {
                return TriageResult.failure("Codex returned a report exceeding the display limit.");
            }
            return TriageResult.success(report);
        } catch (IOException e) {
            return TriageResult.failure("Cannot start or read Codex output ("
                    + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "). Check CODEX_COMMAND, CLI login, and repository permissions.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) terminateProcessTree(process);
            return TriageResult.failure("Codex triage interrupted.");
        } finally {
            deleteQuietly(reportFile);
            deleteQuietly(logFile);
        }
    }

    List<String> commandFor(long issueId, Path reportFile) {
        if (issueId <= 0) throw new IllegalArgumentException("Issue ID must be positive.");
        List<String> args = new ArrayList<>();
        args.add(executable);
        args.add("exec");
        args.add("--ephemeral");
        args.add("--sandbox");
        args.add("read-only");
        args.add("--cd");
        args.add(workspace.toString());
        args.add("--output-last-message");
        args.add(reportFile.toString());
        args.add("$issue-pilot " + issueId);
        return List.copyOf(args);
    }

    private static String diagnostic(Path log) {
        if (log == null) return "(no log)";
        try {
            String body = Files.readString(log, StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) return "(no CLI output)";
            // Last lines are normally the most useful error diagnostics.
            return body.length() > MAX_DIAGNOSTIC_CHARS
                    ? "…" + body.substring(body.length() - MAX_DIAGNOSTIC_CHARS) : body;
        } catch (IOException e) {
            return "(unable to read CLI output)";
        }
    }

    private static void terminateProcessTree(Process process) {
        // Child processes can survive a Windows CLI wrapper; terminate descendants too.
        process.descendants().forEach(child -> {
            try { child.destroyForcibly(); } catch (Exception ignored) { }
        });
        process.destroyForcibly();
        try { process.waitFor(5, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static void deleteQuietly(Path file) {
        if (file == null) return;
        try { Files.deleteIfExists(file); } catch (IOException ignored) { }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    public record TriageStatus(long issueId, String state, String message, Instant updatedAt) {}
    private record TriageResult(boolean success, String message) {
        static TriageResult success(String report) { return new TriageResult(true, report); }
        static TriageResult failure(String error) { return new TriageResult(false, error); }
    }
}
