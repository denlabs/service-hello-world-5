package com.tenyks.helloworld;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class HelloWorldApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SpringLiquibase springLiquibase;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull();
    }

    @Test
    void liquibaseIsConfiguredWithXmlMasterChangelog() {
        assertThat(springLiquibase.getChangeLog())
                .isEqualTo("classpath:db/changelog/db.changelog-master.xml");
    }

    @Test
    void liquibaseChangelogTableIsCreated() {
        Integer count = new JdbcTemplate(dataSource)
                .queryForObject("SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class);
        assertThat(count).isNotNull();
    }
}
