package com.issuepilot.views;

import com.issuepilot.service.CodexTriageService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Local, human-reviewed AI triage action displayed near the issue heading.
 * Results are collapsible and are not saved to the issue database.
 */
public class AiTriagePanel extends VerticalLayout {

    public AiTriagePanel(long issueId, CodexTriageService triageService) {
        setWidthFull();
        setPadding(false);
        setSpacing(false);

        Button runButton = new Button("Run AI Triage");
        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Paragraph status = new Paragraph("Advisory analysis · Human review required");
        status.getStyle().set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout actions = new HorizontalLayout(runButton, status);
        actions.setWidthFull();
        actions.setAlignItems(Alignment.CENTER);
        actions.setFlexGrow(1, status);

        TextArea report = new TextArea();
        report.setReadOnly(true);
        report.setWidthFull();
        report.setMinHeight("350px");
        report.setAriaLabel("AI triage report");

        Details results = new Details("AI Triage Results", report);
        results.setWidthFull();
        results.setOpened(false);
        results.setVisible(false);

        runButton.addClickListener(event -> {
            UI ui = event.getSource().getUI().orElse(null);
            if (ui == null) return;

            runButton.setEnabled(false);
            status.setText("Running Codex analysis…");
            results.setVisible(false);
            results.setOpened(false);
            report.setValue("");

            triageService.analyze(issueId).whenComplete((result, error) -> ui.access(() -> {
                if (!isAttached()) return; // User navigated to another issue.
                runButton.setEnabled(true);
                if (error != null) {
                    status.setText("Unable to complete AI triage. Check the application logs.");
                } else if (!result.success()) {
                    status.setText(result.message());
                } else {
                    status.setText("Analysis complete · Human review required");
                    report.setValue(result.message());
                    results.setVisible(true);
                    results.setOpened(true);
                }
            }));
        });

        add(actions, results);
    }
}
