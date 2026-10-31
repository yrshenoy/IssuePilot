package com.issuepilot.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Local-only bridge to the already-authenticated Codex CLI.
 * No shell, free-form prompt input, or write-enabled sandbox is permitted.
 */
@Service
public class CodexTriageService {
    private static final Duration TIMEOUT = Duration.ofMinutes(3);
    private static final int MAX_REPORT_CHARS = 120_000;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "issuepilot-codex");
        thread.setDaemon(true);
        return thread;
    });
    private final Semaphore activeRun = new Semaphore(1);
    private final String executable;
    private final Path workspace;

    public CodexTriageService(
            @Value("${issuepilot.codex.executable:}") String configuredExecutable,
            @Value("${issuepilot.codex.workspace:}") String configuredWorkspace) {
        this.executable = configuredExecutable.isBlank()
                ? (System.getProperty("os.name").toLowerCase().contains("win") ? "codex.cmd" : "codex")
                : configuredExecutable;
        this.workspace = configuredWorkspace.isBlank()
                ? Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
                : Path.of(configuredWorkspace).toAbsolutePath().normalize();
    }

    public CompletableFuture<TriageResult> analyze(long issueId) {
        if (issueId <= 0) {
            return CompletableFuture.completedFuture(TriageResult.failure("Invalid issue ID."));
        }
        if (!activeRun.tryAcquire()) {
            return CompletableFuture.completedFuture(
                    TriageResult.failure("Another AI triage is running. Try again when it finishes."));
        }
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return execute(issueId);
                } finally {
                    activeRun.release();
                }
            }, executor);
        } catch (RuntimeException e) {
            activeRun.release();
            return CompletableFuture.completedFuture(
                    TriageResult.failure("Unable to schedule Codex triage."));
        }
    }

    private TriageResult execute(long issueId) {
        Path reportFile = null;
        Path logFile = null;
        Process process = null;
        try {
            if (!Files.isRegularFile(workspace.resolve(".agents/skills/issue-pilot/SKILL.md"))) {
                return TriageResult.failure(
                        "IssuePilot skill not found. Set ISSUEPILOT_CODEX_WORKSPACE to the GitHub project directory.");
            }
            reportFile = Files.createTempFile("issuepilot-report-", ".txt");
            logFile = Files.createTempFile("issuepilot-codex-", ".log");

            ProcessBuilder builder = new ProcessBuilder(commandFor(issueId, reportFile));
            builder.directory(workspace.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(logFile.toFile());
            process = builder.start();

            boolean finished = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                return TriageResult.failure("Codex exceeded the three-minute timeout. Try again.");
            }
            if (process.exitValue() != 0) {
                return TriageResult.failure("Codex exited with status " + process.exitValue()
                        + ". Confirm the CLI is installed, authenticated and able to reach localhost:8080. "
                        + "Run Codex in a terminal for diagnostic details.");
            }
            String report = Files.readString(reportFile, StandardCharsets.UTF_8).trim();
            if (report.isEmpty()) {
                return TriageResult.failure("Codex finished without returning a report.");
            }
            if (report.length() > MAX_REPORT_CHARS) {
                return TriageResult.failure("Codex returned an unexpectedly large report.");
            }
            return TriageResult.success(report);
        } catch (IOException e) {
            return TriageResult.failure(
                    "Cannot launch Codex. Check that the CLI is installed and on the application's PATH "
                    + "or set ISSUEPILOT_CODEX_EXECUTABLE to the CLI path.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) process.destroyForcibly();
            return TriageResult.failure("Codex triage was interrupted.");
        } finally {
            deleteQuietly(reportFile);
            deleteQuietly(logFile);
        }
    }

    List<String> commandFor(long issueId, Path reportFile) {
        if (issueId <= 0) throw new IllegalArgumentException("issueId must be positive");
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("exec");
        command.add("--ephemeral");
        command.add("--sandbox");
        command.add("read-only");
        command.add("--cd");
        command.add(workspace.toString());
        command.add("--output-last-message");
        command.add(reportFile.toString());
        command.add("$issue-pilot " + issueId);
        return List.copyOf(command);
    }

    private static void deleteQuietly(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Temporary output is best-effort cleanup.
            }
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    public record TriageResult(boolean success, String message) {
        public static TriageResult success(String report) {
            return new TriageResult(true, report);
        }
        public static TriageResult failure(String error) {
            return new TriageResult(false, error);
        }
    }
}
