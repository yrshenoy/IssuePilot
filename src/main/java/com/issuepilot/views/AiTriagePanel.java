package com.issuepilot.views;

import com.issuepilot.service.CodexTriageService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * A per-issue, local-only Codex triage panel.
 * The generated report is advisory and is never persisted or applied to an issue.
 */
public class AiTriagePanel extends VerticalLayout {

    public AiTriagePanel(long issueId, CodexTriageService triageService) {
        setWidthFull();
        setPadding(false);
        add(new H3("AI-assisted triage"));

        Paragraph status = new Paragraph("Codex analyzes the selected issue and suggests next steps for human review.");
        Button runButton = new Button("Run AI Triage");
        TextArea report = new TextArea("Codex report");
        report.setReadOnly(true);
        report.setWidthFull();
        report.setMinHeight("350px");
        report.setVisible(false);

        runButton.addClickListener(event -> {
            UI ui = event.getSource().getUI().orElse(null);
            if (ui == null) return;

            runButton.setEnabled(false);
            status.setText("Running Codex analysis…");
            report.setVisible(false);
            report.clear();

            triageService.analyze(issueId).whenComplete((result, error) -> ui.access(() -> {
                if (!isAttached()) return; // The user may have selected a different issue.
                runButton.setEnabled(true);
                if (error != null) {
                    status.setText("Unable to complete AI triage. Check the application logs.");
                    return;
                }
                if (!result.success()) {
                    status.setText(result.message());
                    return;
                }
                status.setText("Analysis complete — advisory findings; human review required.");
                report.setValue(result.message());
                report.setVisible(true);
            }));
        });

        add(runButton, status, report);
    }
}
