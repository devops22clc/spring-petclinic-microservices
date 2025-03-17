package org.springframework.samples.petclinic.customers.web.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OwnerEntityMapperTest {

    private OwnerEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OwnerEntityMapper();
    }

    @Test
    void shouldMapOwnerRequestToOwner() {
        // given
        String address = "123 Main St";
        String city = "Springfield";
        String telephone = "555-1234";
        String firstName = "John";
        String lastName = "Doe";

        OwnerRequest request = new OwnerRequest(firstName, lastName, address, city, telephone);
        Owner owner = new Owner();

        // when
        Owner mappedOwner = mapper.map(owner, request);

        // then
        assertEquals(address, mappedOwner.getAddress());
        assertEquals(city, mappedOwner.getCity());
        assertEquals(telephone, mappedOwner.getTelephone());
        assertEquals(firstName, mappedOwner.getFirstName());
        assertEquals(lastName, mappedOwner.getLastName());
    }

    @Test
    void shouldOverrideExistingOwnerValues() {
        // given
        Owner existingOwner = new Owner();
        existingOwner.setAddress("456 Oak Ave");
        existingOwner.setCity("Old City");
        existingOwner.setTelephone("555-5678");
        existingOwner.setFirstName("Jane");
        existingOwner.setLastName("Smith");

        String newAddress = "789 Pine St";
        String newCity = "New City";
        String newTelephone = "555-9876";
        String newFirstName = "Bob";
        String newLastName = "Johnson";

        OwnerRequest request = new OwnerRequest(newFirstName, newLastName, newAddress, newCity, newTelephone);

        // when
        Owner mappedOwner = mapper.map(existingOwner, request);

        // then
        assertEquals(newAddress, mappedOwner.getAddress());
        assertEquals(newCity, mappedOwner.getCity());
        assertEquals(newTelephone, mappedOwner.getTelephone());
        assertEquals(newFirstName, mappedOwner.getFirstName());
        assertEquals(newLastName, mappedOwner.getLastName());
    }

    @Test
    void shouldReturnSameOwnerInstance() {
        // given
        Owner originalOwner = new Owner();
        OwnerRequest request = new OwnerRequest("First", "Last", "Address", "City", "Phone");

        // when
        Owner mappedOwner = mapper.map(originalOwner, request);

        // then
        assertEquals(originalOwner, mappedOwner, "Mapper should return the same Owner instance");
    }
}