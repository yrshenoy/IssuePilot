
package com.issuepilot.views;

import com.issuepilot.model.Issue;
import com.issuepilot.repository.IssueRepository;
import com.issuepilot.service.CodexTriageService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import java.util.Optional;

@Route("issue")
public class IssueDetailView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final IssueRepository repository;
    private final CodexTriageService triageService;

    public IssueDetailView(IssueRepository repository, CodexTriageService triageService) {
        this.repository = repository;
        this.triageService = triageService;
    }

    @Override
    public void setParameter(BeforeEvent event, Long issueId) {

        removeAll();

        Optional<Issue> result = repository.findById(issueId);

        if (result.isEmpty()) {
            add(new H2("Issue not found: " + issueId));
            return;
        }

        Issue issue = result.get();

        add(new H2("Issue #" + issue.getId()));
        add(new H3(issue.getSubject()));

        add(new Paragraph("Type: " + issue.getIssueType()));
        add(new Paragraph("Status: " + issue.getStatus()));

        // Keep the primary triage action immediately beneath the issue heading and status.
        add(new AiTriagePanel(issue.getId(), triageService));
        add(new Paragraph("Created: " + issue.getCreatedAt()));
        add(new Paragraph("Closed: " + issue.getClosedAt()));

        add(new H3("Description"));
        add(new Paragraph(issue.getDescription()));

        add(new H3("Resolution"));
        add(new Paragraph("Type: " + issue.getResolutionType()));
        add(new Paragraph(issue.getResolutionNotes() == null
                ? "Not resolved"
                : issue.getResolutionNotes()));

        add(new H3("Files Changed"));
        add(new Paragraph(issue.getFilesChanged() == null
                ? "None"
                : issue.getFilesChanged()));

    }
}
