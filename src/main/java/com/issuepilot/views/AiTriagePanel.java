package com.issuepilot.views;

import com.issuepilot.service.CodexTriageService;
import com.issuepilot.service.CodexTriageService.TriageStatus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.shared.Registration;

/**
 * Triage status and report are read from a per-issue service, surviving navigation
 * and refresh for the lifetime of this local application process.
 */
public class AiTriagePanel extends VerticalLayout {
    private final long issueId;
    private final CodexTriageService triageService;
    private final Button runButton = new Button("Run AI Triage");
    private final Paragraph status = new Paragraph();
    private final TextArea report = new TextArea("Codex report");
    private final Details results = new Details("AI Triage Results", report);
    private String lastState = "";

    public AiTriagePanel(long issueId, CodexTriageService triageService) {
        this.issueId = issueId;
        this.triageService = triageService;
        setWidthFull();
        setPadding(false);
        setSpacing(false);

        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");
        status.getStyle().set("white-space", "pre-wrap");

        HorizontalLayout actions = new HorizontalLayout(runButton, status);
        actions.setWidthFull();
        actions.setAlignItems(Alignment.CENTER);
        actions.setFlexGrow(1, status);

        report.setReadOnly(true);
        report.setWidthFull();
        report.setMinHeight("350px");
        results.setWidthFull();
        results.setOpened(false);
        results.setVisible(false);

        runButton.addClickListener(e -> {
            triageService.start(issueId);
            refreshStatus();
        });

        add(actions, results);
        addAttachListener(event -> {
            var ui = event.getUI();
            ui.setPollInterval(1000); // Refresh status even after navigating or reloading.
            Registration registration = ui.addPollListener(poll -> {
                if (isAttached()) refreshStatus();
            });
            Registration detach = addDetachListener(d -> registration.remove());
            // The detach listener is tied to this component instance and will
            // be discarded along with the view.
            refreshStatus();
        });
    }

    private void refreshStatus() {
        TriageStatus current = triageService.status(issueId);
        Long busyIssue = triageService.activeIssueId();
        boolean running = "RUNNING".equals(current.state());
        boolean otherIssueRunning = busyIssue != null && busyIssue != issueId;
        runButton.setEnabled(!running && !otherIssueRunning);

        if (running) {
            status.setText("Analyzing issue #" + issueId
                    + "… This may take up to 3 minutes. You can switch issues.");
        } else if (otherIssueRunning) {
            status.setText("Issue #" + busyIssue + " is being analyzed. "
                    + "Return to this issue after it finishes to start a new run.");
        } else if ("COMPLETED".equals(current.state())) {
            status.setText("Analysis complete · Human review required");
        } else {
            status.setText(current.message());
        }

        String newState = current.state() + "@" + current.updatedAt();
        if (!newState.equals(lastState)) {
            lastState = newState;
            if ("COMPLETED".equals(current.state())) {
                report.setValue(current.message());
                results.setVisible(true);
                results.setOpened(true);
            } else if ("FAILED".equals(current.state())) {
                report.setValue("AI triage did not complete:\n\n" + current.message());
                results.setVisible(true);
                results.setOpened(true);
            } else if ("RUNNING".equals(current.state())) {
                results.setVisible(false);
                results.setOpened(false);
            }
        }
    }
}
