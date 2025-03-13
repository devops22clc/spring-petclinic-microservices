package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.visits.VisitsServiceApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VisitsServiceApplicationTest {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }

    @Test
    void mainMethodRunsSuccessfully() {
        String[] args = {};
        VisitsServiceApplication.main(args);
    }
}
