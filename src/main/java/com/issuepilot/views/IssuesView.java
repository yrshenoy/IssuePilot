
package com.issuepilot.views;

import com.issuepilot.model.Issue;
import com.issuepilot.repository.IssueRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

import com.issuepilot.model.IssueType;
import com.issuepilot.model.IssueStatus;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.Locale;


@Route("issues")
public class IssuesView extends VerticalLayout {

    private final VerticalLayout detailsPanel =
            new VerticalLayout();

    public IssuesView(IssueRepository repository) {

        setSizeFull();
        setPadding(true);

        add(new H2("IssuePilot - Issues"));

        // Top half: Issues grid
        Grid<Issue> grid = new Grid<>(Issue.class, false);

        grid.addColumn(Issue::getId)
                .setHeader("ID")
                .setSortable(true);

        grid.addColumn(Issue::getSubject)
                .setHeader("Subject")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Issue::getIssueType)
                .setHeader("Type");

        grid.addColumn(Issue::getStatus)
                .setHeader("Status");

        grid.addColumn(Issue::getAssignedTeam)
                .setHeader("Assigned Team");

        grid.addColumn(Issue::getCreatedAt)
                .setHeader("Created");

        GridListDataView<Issue> dataView =
                grid.setItems(repository.findAll());

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search ID or subject");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        ComboBox<IssueType> typeFilter =
                new ComboBox<>("Issue Type");
        typeFilter.setItems(IssueType.values());
        typeFilter.setPlaceholder("All types");
        typeFilter.setClearButtonVisible(true);

        ComboBox<IssueStatus> statusFilter =
                new ComboBox<>("Status");
        statusFilter.setItems(IssueStatus.values());
        statusFilter.setPlaceholder("All statuses");
        statusFilter.setClearButtonVisible(true);

// Apply all filters together.
        dataView.addFilter(issue -> {

            String search = searchField.getValue()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            boolean matchesSearch =
                    search.isEmpty()
                            || String.valueOf(issue.getId()).contains(search)
                            || (issue.getSubject() != null
                            && issue.getSubject()
                            .toLowerCase(Locale.ROOT)
                            .contains(search));

            boolean matchesType =
                    typeFilter.getValue() == null
                            || typeFilter.getValue() == issue.getIssueType();

            boolean matchesStatus =
                    statusFilter.getValue() == null
                            || statusFilter.getValue() == issue.getStatus();

            return matchesSearch && matchesType && matchesStatus;
        });

// Refresh the grid whenever a filter changes.
        searchField.addValueChangeListener(
                event -> dataView.refreshAll());

        typeFilter.addValueChangeListener(
                event -> dataView.refreshAll());

        statusFilter.addValueChangeListener(
                event -> dataView.refreshAll());

        HorizontalLayout filterBar = new HorizontalLayout(
                searchField, typeFilter, statusFilter);

        filterBar.setWidthFull();
        filterBar.setAlignItems(
                HorizontalLayout.Alignment.END);

        grid.setSizeFull();

        // Bottom half: Selected issue details
        detailsPanel.setPadding(true);
        detailsPanel.setSpacing(false);
        detailsPanel.setSizeFull();
        detailsPanel.getStyle().set("overflow", "auto");

        detailsPanel.add(
                new Paragraph("Select an issue to view its details.")
        );

        // Update bottom panel when selecting a row
        grid.addSelectionListener(event ->
                event.getFirstSelectedItem()
                        .ifPresent(this::showIssueDetails)
        );

        // Vertical split: grid above, details below
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setOrientation(
                SplitLayout.Orientation.VERTICAL
        );

        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(50);

        splitLayout.addToPrimary(grid);
        splitLayout.addToSecondary(detailsPanel);

        add(filterBar);
        add(splitLayout);
        expand(splitLayout);
    }

    private void showIssueDetails(Issue issue) {

        detailsPanel.removeAll();

        detailsPanel.add(
                new H3("Issue #" + issue.getId()
                        + " - " + issue.getSubject())
        );

        detailsPanel.add(new Paragraph(
                "Type: " + issue.getIssueType()
                        + " | Status: " + issue.getStatus()
                        + " | Team: " + value(issue.getAssignedTeam())
        ));

        detailsPanel.add(new Paragraph(
                "Created: " + value(issue.getCreatedAt())
                        + " | Closed: " + value(issue.getClosedAt())
        ));

        detailsPanel.add(new H3("Description"));
        detailsPanel.add(new Paragraph(
                value(issue.getDescription())
        ));

        detailsPanel.add(new H3("Resolution"));
        detailsPanel.add(new Paragraph(
                "Resolution Type: "
                        + value(issue.getResolutionType())
        ));
        detailsPanel.add(new Paragraph(
                value(issue.getResolutionNotes())
        ));

        detailsPanel.add(new H3("Files Changed"));
        detailsPanel.add(new Paragraph(
                value(issue.getFilesChanged())
        ));
    }

    private String value(Object value) {
        return value == null ? "—" : value.toString();
    }
}
