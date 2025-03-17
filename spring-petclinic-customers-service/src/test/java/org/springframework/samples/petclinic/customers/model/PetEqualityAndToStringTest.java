package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetEqualityAndToStringTest {

    private Pet pet1;
    private Pet pet2;
    private Owner owner;
    private PetType petType;
    private Date birthDate;

    @BeforeEach
    void setUp() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        birthDate = dateFormat.parse("2020-01-01");

        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Anytown");
        owner.setTelephone("555-1234");

        petType = new PetType();
        petType.setId(1);
        petType.setName("dog");

        // Create first pet
        pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Fluffy");
        pet1.setBirthDate(birthDate);
        pet1.setType(petType);
        pet1.setOwner(owner);

        // Create identical pet
        pet2 = new Pet();
        pet2.setId(2);
        pet2.setName("Fluffy");
        pet2.setBirthDate(birthDate);
        pet2.setType(petType);
        pet2.setOwner(owner);
    }

    @Test
    void testToString() {
        String toString = pet1.toString();
        assertTrue(toString.contains("id = " + pet1.getId()));
        assertTrue(toString.contains("name = " + "'" + pet1.getName() + "'"));
        assertTrue(toString.contains("birthDate = " + pet1.getBirthDate()));
        assertTrue(toString.contains("ownerFirstname = " + "'" + pet1.getOwner().getFirstName() + "'"));
        assertTrue(toString.contains("ownerLastname = " + "'" + pet1.getOwner().getLastName() + "'"));
        assertTrue(toString.contains("type = " + "'" + pet1.getType().getName() + "'"));
    }
}
