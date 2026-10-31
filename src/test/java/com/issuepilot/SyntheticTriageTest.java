package com.issuepilot;

import com.issuepilot.api.IssueController;
import com.issuepilot.repository.IssueRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "spring.datasource.url=jdbc:h2:mem:issuepilot-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "vaadin.launch-browser=false"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SyntheticTriageTest {
    @Autowired DataSource dataSource;
    @Autowired IssueRepository repository;
    @Autowired IssueController controller;

    @BeforeAll
    void loadSyntheticCorpus() {
        var seed = new ResourceDatabasePopulator(
                new FileSystemResource("test-data/issuepilot_seed_h2.sql"));
        seed.execute(dataSource);
        seed.execute(dataSource); // Re-running must update, not duplicate, fixture rows.
    }

    @Test
    void seedIsRepeatableAndPreservesEvaluationEvidence() {
        assertThat(repository.count()).isEqualTo(48);
        assertThat(controller.getAllIssues()).hasSize(48);
        var incoming = controller.getIssueById(2401L);
        assertThat(incoming.getStatusCode().value()).isEqualTo(200);
        assertThat(incoming.getBody()).isNotNull();
        assertThat(incoming.getBody().getAssignedTeam()).isNull();
        assertThat(incoming.getBody().getStatus().name()).isEqualTo("OPEN");
        assertThat(repository.findById(2007L).orElseThrow().getAssignedTeam())
                .isEqualTo("Shipping Engineering");
        assertThat(repository.findById(2039L).orElseThrow().getStatus().name())
                .isEqualTo("CLOSED");
    }

    @Test
    void unknownIssueReturnsNotFound() {
        assertThat(controller.getIssueById(999999L).getStatusCode().value()).isEqualTo(404);
    }
}
