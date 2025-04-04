package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomersServiceApplicationTest {
     @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }

    @Test
    void mainMethodRunsSuccessfully() {
        String[] args = {};
        CustomersServiceApplication.main(args);
    }
}
