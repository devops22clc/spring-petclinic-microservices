package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Test
    void shouldFetchVisitsByPetId() throws Exception {
        given(visitRepository.findByPetId(111))
            .willReturn(asList(
                Visit.VisitBuilder.aVisit().id(1).petId(111).build(),
                Visit.VisitBuilder.aVisit().id(2).petId(111).build()
            ));

        mvc.perform(get("/owners/*/pets/111/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldCreateVisit() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit().id(1).petId(111).description("Annual Checkup").build();
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        mvc.perform(post("/owners/*/pets/111/visits")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"description\":\"Annual Checkup\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.petId").value(111))
                .andExpect(jsonPath("$.description").value("Annual Checkup"));
    }

    @Test
    void shouldFetchVisitsByPetIds() throws Exception {
        given(visitRepository.findByPetIdIn(asList(111, 222)))
            .willReturn(asList(
                Visit.VisitBuilder.aVisit().id(1).petId(111).build(),
                Visit.VisitBuilder.aVisit().id(2).petId(222).build()
            ));

        mvc.perform(get("/pets/visits?petId=111,222"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[1].id").value(2));
    }
}
