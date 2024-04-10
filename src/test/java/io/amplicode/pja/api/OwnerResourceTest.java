package io.amplicode.pja.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.amplicode.pja.model.Owner;
import io.amplicode.pja.repository.OwnerRepository;
import io.amplicode.pja.repository.PetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.skip.in-tests=false"
})
@AutoConfigureMockMvc
class OwnerResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OwnerRepository ownerRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private final List<Long> afterTestToDeleteIds = new ArrayList<>();

    @Autowired
    private PetRepository petRepository;

    @BeforeEach
    void setUp() {
        afterTestToDeleteIds.clear();
    }

    @AfterEach
    void tearDown() {
        if (!afterTestToDeleteIds.isEmpty()) {
            ownerRepository.deleteAllByIdInBatch(afterTestToDeleteIds);
        }
    }

    @Test
    public void getListQueryAndSort() throws Exception {
        mockMvc.perform(get("/rest/owners")
                        .param("q", "ro")
                        .param("sort", "city")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].firstName", is("David")))
                .andExpect(jsonPath("$.content[0].petIds", hasSize(1)))
                .andExpect(jsonPath("$.content[0].petIds[0]", is(11)))
                .andExpect(jsonPath("$.content[1].firstName", is("Eduardo")))
                .andExpect(jsonPath("$.content[1].petIds", hasSize(2)))
                .andExpect(jsonPath("$.content[1].petIds[0]", is(4)))
                .andExpect(jsonPath("$.content[1].petIds[1]", is(3)))
                .andExpect(jsonPath("$.content[2].firstName", is("Harold")))
                .andExpect(jsonPath("$.content[2].petIds", hasSize(1)))
                .andExpect(jsonPath("$.content[2].petIds[0]", is(5)));
    }

    @Test
    public void getListFilterByQueryAll() throws Exception {
        mockMvc.perform(get("/rest/owners")
                        .param("q", "ro")
                        .param("address", "black")
                        .param("city", "mad")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(1)))
                .andExpect(jsonPath("$.content[0].firstName", is("David")));
    }

    @Test
    public void getListSize5Page2SortName() throws Exception {
        mockMvc.perform(get("/rest/owners")
                        .param("size", "5")
                        .param("page", "1")
                        .param("sort", "firstName")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(5)))
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(true)))
                .andExpect(jsonPath("$.content[0].firstName", is("Harold")))
                .andExpect(jsonPath("$.content[1].firstName", is("Jean")))
                .andExpect(jsonPath("$.content[2].firstName", is("Jeff")))
                .andExpect(jsonPath("$.content[3].firstName", is("Maria")))
                .andExpect(jsonPath("$.content[4].firstName", is("Peter")));
    }

    @Test
    public void getOne() throws Exception {
        mockMvc.perform(get("/rest/owners/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Eduardo")))
                .andExpect(jsonPath("$.lastName", is("Rodriquez")))
                .andExpect(jsonPath("$.address", is("2693 Commerce St.")))
                .andExpect(jsonPath("$.city", is("McFarland")))
                .andExpect(jsonPath("$.telephone", is("6085558763")));
    }

    @Test
    public void getOneNotFound() throws Exception {
        mockMvc.perform(get("/rest/owners/" + Integer.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.title", is("Not Found")),
                        jsonPath("$.detail", is("Entity with id `%s` not found".formatted(Integer.MAX_VALUE)))
                );
    }

    @Test
    public void getMany() throws Exception {
        mockMvc.perform(get("/rest/owners/by-ids")
                        .param("ids", "1", "3", "5", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[*].firstName", containsInAnyOrder("George", "Eduardo", "Peter", "Jeff"))
                );
    }

    @Test
    public void createAndDelete() throws Exception {
        MvcResult result = mockMvc.perform(post("/rest/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"firstName": "Ivan",
                        	"lastName": "Ivanov",
                        	"address": "Gastelo 43a",
                        	"city": "Samara",
                        	"telephone": "9271111111"
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.firstName", is("Ivan")),
                jsonPath("$.lastName", is("Ivanov")),
                jsonPath("$.address", is("Gastelo 43a")),
                jsonPath("$.city", is("Samara")),
                jsonPath("$.telephone", is("9271111111"))
        ).andReturn();

        //drop in TearDown
        String responseContent = result.getResponse().getContentAsString();
        Map<String, Object> responceMap = objectMapper.readValue(responseContent,
                new TypeReference<HashMap<String, Object>>() {
                });
        long id = Long.parseLong(responceMap.get("id").toString());
        afterTestToDeleteIds.add(id);
    }

    @Test
    public void purePut() throws Exception {
        Long ownerId = createAndSaveAlexOwner();

        mockMvc.perform(put("/rest/owners/" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "firstName": "Alex",
                        	"lastName": "Ivanov",
                        	"address": "Gastelo 43a",
                        	"city": "Samara",
                        	"telephone": "9271112233" 
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.firstName", is("Alex")),
                jsonPath("$.lastName", is("Ivanov")),
                jsonPath("$.address", is("Gastelo 43a")),
                jsonPath("$.city", is("Samara")),
                jsonPath("$.telephone", is("9271112233"))
        ).andReturn();
    }

    @Test
    public void patchByDto() throws Exception {
        patchTest("by-dto");
    }

    @Test
    public void patchByJson() throws Exception {
        patchTest("by-json");
    }

    @Test
    public void patchByStarter() throws Exception {
        patchTest("by-starter");
    }

    @Test
    public void patchByDtoValidation() throws Exception {
        patchValidationTest("by-dto", "Failed to bind request");
    }

    @Test
    public void patchByJsonNodeValidation() throws Exception {
        patchValidationTest("by-json", "Failed to bind request");
    }

//    @Test
    public void patchByStarterValidation() throws Exception {
        patchValidationTest("by-starter", "Invalid patch request content.");
    }

    private void patchTest(String patchUrl) throws Exception {
        Long ownerId = createAndSaveAlexOwner();

        mockMvc.perform(patch("/rest/owners/" + patchUrl + "/" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"lastName": "Ivanov",
                        	"address": "Gastelo 43a"
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.firstName", is("Alex")),
                jsonPath("$.lastName", is("Ivanov")),
                jsonPath("$.address", is("Gastelo 43a")),
                jsonPath("$.city", is("Samara")),
                jsonPath("$.telephone", is("9271112233"))
        ).andReturn();
    }

    private void patchValidationTest(String patchUrl, String validationError) throws Exception {
        Long ownerId = createAndSaveAlexOwner();

        mockMvc.perform(patch("/rest/owners/" + patchUrl + "/" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"telephone": "+79271112233"
                        }""")
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.detail", is(validationError))
        );
    }

    @Test
    public void deleteTest() throws Exception {
        Long ownerId = createAndSaveAlexOwner();

        mockMvc.perform(delete("/rest/owners/" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.firstName", is("Alex")),
                jsonPath("$.lastName", is("Alexov")),
                jsonPath("$.address", is("Gastelo")),
                jsonPath("$.city", is("Samara")),
                jsonPath("$.telephone", is("9271112233"))
        ).andReturn();
    }

    @Test
    public void deleteMany() throws Exception {
        Long alexId = createAndSaveAlexOwner();
        Long ivanId = createAndSaveIvanOwner();

        mockMvc.perform(delete("/rest/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .param("ids", alexId.toString(), ivanId.toString())
        ).andExpectAll(
                status().isOk(),
                jsonPath("$[0]", is(Math.toIntExact(alexId))),
                jsonPath("$[1]", is(Math.toIntExact(ivanId)))
        ).andReturn();
    }

    private Long createAndSaveAlexOwner() {
        Owner owner = new Owner();
        owner.setFirstName("Alex");
        owner.setLastName("Alexov");
        owner.setAddress("Gastelo");
        owner.setCity("Samara");
        owner.setTelephone("9271112233");
        Owner savedOwner = ownerRepository.save(owner);
        Long ownerId = savedOwner.getId();
        afterTestToDeleteIds.add(ownerId);
        return ownerId;
    }

    private Long createAndSaveIvanOwner() {
        Owner owner = new Owner();
        owner.setFirstName("Ivan");
        owner.setLastName("Ivanov");
        owner.setAddress("My Address");
        owner.setCity("My City");
        owner.setTelephone("0001112233");
        Owner savedOwner = ownerRepository.save(owner);
        Long ownerId = savedOwner.getId();
        afterTestToDeleteIds.add(ownerId);
        return ownerId;
    }
}
