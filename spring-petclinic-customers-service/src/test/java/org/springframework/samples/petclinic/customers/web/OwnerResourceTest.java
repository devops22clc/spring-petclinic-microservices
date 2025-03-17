package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerResourceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private OwnerEntityMapper ownerEntityMapper;

    @InjectMocks
    private OwnerResource ownerResource;

    private Owner owner;
    private OwnerRequest ownerRequest;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Boston");
        owner.setTelephone("555-1234");

        ownerRequest = new OwnerRequest("John", "Doe", "123 Main St", "Boston", "555-1234");
    }

    @Test
    void testCreateOwner() {
        // given
        when(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).thenReturn(owner);
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        // when
        Owner createdOwner = ownerResource.createOwner(ownerRequest);

        // then
        assertEquals(owner, createdOwner);
        verify(ownerEntityMapper).map(any(Owner.class), eq(ownerRequest));
        verify(ownerRepository).save(owner);
    }

    @Test
    void testFindOwner() {
        // given
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        // when
        Optional<Owner> foundOwner = ownerResource.findOwner(1);

        // then
        assertEquals(Optional.of(owner), foundOwner);
        verify(ownerRepository).findById(1);
    }

    @Test
    void testFindAll() {
        // given
        List<Owner> owners = Arrays.asList(owner);
        when(ownerRepository.findAll()).thenReturn(owners);

        // when
        List<Owner> foundOwners = ownerResource.findAll();

        // then
        assertEquals(owners, foundOwners);
        verify(ownerRepository).findAll();
    }

    @Test
    void testUpdateOwner() {
        // given
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).thenReturn(owner);

        // when
        ownerResource.updateOwner(1, ownerRequest);

        // then
        verify(ownerRepository).findById(1);
        verify(ownerEntityMapper).map(owner, ownerRequest);
        verify(ownerRepository).save(owner);
    }

    @Test
    void testUpdateOwnerNotFound() {
        // given
        when(ownerRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> ownerResource.updateOwner(1, ownerRequest));
        verify(ownerRepository).findById(1);
        verify(ownerEntityMapper, never()).map(any(), any());
        verify(ownerRepository, never()).save(any());
    }
}