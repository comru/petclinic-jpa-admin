package io.amplicode.pja.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.amplicode.pja.model.Owner;
import io.amplicode.pja.model.Pet;
import io.amplicode.pja.model.PetType;
import io.amplicode.pja.repository.PetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.skip.in-tests=false"
})
@AutoConfigureMockMvc
class PetResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PetRepository petRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private final List<Long> afterTestToDeleteIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        afterTestToDeleteIds.clear();
    }

    @AfterEach
    void tearDown() {
        if (!afterTestToDeleteIds.isEmpty()) {
            petRepository.deleteAllByIdInBatch(afterTestToDeleteIds);
        }
    }

    @Test
    public void getListQueryAndSort() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("q", "Lu")
                        .param("sort", "birthDate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].typeId", is(5)))
                .andExpect(jsonPath("$.content[0].birthDate", is("1999-08-06")))
                .andExpect(jsonPath("$.content[1].typeId", is(2)))
                .andExpect(jsonPath("$.content[1].birthDate", is("2000-06-24")));
    }

    @Test
    public void getListQueryByPetName() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("q", "dog")
                        .param("sort", "name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[0].name", is("Jewel")))
                .andExpect(jsonPath("$.content[1].name", is("Lucky")))
                .andExpect(jsonPath("$.content[2].name", is("Mulligan")))
                .andExpect(jsonPath("$.content[3].name", is("Rosy")));
    }

    @Test
    public void getListFilterByQueryAll() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("ownerId", "3")
                        .param("q", "dog")
                        .param("birthDateGreaterThan", "2000-01-01")
                        .param("birthDateLessThan", "2000-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Jewel")));
    }

    @Test
    public void getListQueryByOwnerFirstNameOrOwnerLastName() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("ownerFirstName", "et")
                        .param("ownerLastName", "is")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(2)));
    }


    @Test
    public void getListQueryByBirthDate() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("birthDateGreaterThan", "2000-01-01")
                        .param("birthDateLessThan", "2000-12-31")
                        .param("sort", "name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(6)))
                .andExpect(jsonPath("$.content[0].name", is("Freddy")))
                .andExpect(jsonPath("$.content[1].name", is("George")))
                .andExpect(jsonPath("$.content[2].name", is("Iggy")))
                .andExpect(jsonPath("$.content[3].name", is("Jewel")))
                .andExpect(jsonPath("$.content[4].name", is("Leo")))
                .andExpect(jsonPath("$.content[5].name", is("Lucky")));
    }

    @Test
    public void getListFilterByOwnerId() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("ownerId", "6")
                        .param("sort", "name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.content[0].name", is("Max")))
                .andExpect(jsonPath("$.content[0].ownerId", is(6)))
                .andExpect(jsonPath("$.content[1].name", is("Samantha")))
                .andExpect(jsonPath("$.content[1].ownerId", is(6)));
    }

    @Test
    public void getListFilterByQueryAndOwnerId() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("ownerId", "10")
                        .param("q", "Lu")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(1)))
                .andExpect(jsonPath("$.content[0].typeId", is(2)));
    }

    @Test
    public void getListSize5SortName() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("size", "5")
                        .param("sort", "name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(5)))
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(13)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)))
                .andExpect(jsonPath("$.content[0].name", is("Basil")))
                .andExpect(jsonPath("$.content[1].name", is("Freddy")))
                .andExpect(jsonPath("$.content[2].name", is("George")))
                .andExpect(jsonPath("$.content[3].name", is("Iggy")))
                .andExpect(jsonPath("$.content[4].name", is("Jewel")));
    }

    @Test
    public void getListSize5Page2SortName() throws Exception {
        mockMvc.perform(get("/rest/pets")
                        .param("size", "5")
                        .param("page", "1")
                        .param("sort", "name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(5)))
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(13)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(false)))
                .andExpect(jsonPath("$.content[0].name", is("Leo")))
                .andExpect(jsonPath("$.content[1].name", is("Lucky")))
                .andExpect(jsonPath("$.content[2].name", is("Lucky")))
                .andExpect(jsonPath("$.content[3].name", is("Max")))
                .andExpect(jsonPath("$.content[4].name", is("Mulligan")));
    }

    @Test
    public void getOne() throws Exception {
        mockMvc.perform(get("/rest/pets/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Leo")))
                .andExpect(jsonPath("$.ownerId", is(1)))
                .andExpect(jsonPath("$.typeId", is(1)))
                .andExpect(jsonPath("$.birthDate", is("2000-09-07")));
    }

    @Test
    public void getOneNotFound() throws Exception {
        mockMvc.perform(get("/rest/pets/" + Integer.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.title", is("Not Found")),
                        jsonPath("$.detail", is("Entity with id `%s` not found".formatted(Integer.MAX_VALUE)))
                );
    }

    @Test
    public void getMany() throws Exception {
        mockMvc.perform(get("/rest/pets/by-ids")
                        .param("ids", "1", "3", "5", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].name", is("Leo")),
                        jsonPath("$[1].name", is("Rosy")),
                        jsonPath("$[2].name", is("Iggy")),
                        jsonPath("$[3].name", is("Samantha")),
                        jsonPath("$[3].birthDate").doesNotExist()
                );
    }

    @Test
    public void createAndDelete() throws Exception {
        MvcResult result = mockMvc.perform(post("/rest/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"name": "Buddy",
                        	"birthDate": "2020-04-01",
                        	"typeId": 1,
                        	"ownerId": 1
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name", is("Buddy")),
                jsonPath("$.ownerId", is(1)),
                jsonPath("$.typeId", is(1)),
                jsonPath("$.birthDate", is("2020-04-01"))
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
    public void updateName() throws Exception {
        Long petId = createAndSaveBuddyPet();

        mockMvc.perform(put("/rest/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"name": "New Buddy Name"
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name", is("New Buddy Name")),
                jsonPath("$.ownerId", is(1)),
                jsonPath("$.typeId", is(1)),
                jsonPath("$.birthDate", is("2020-04-01"))
        ).andReturn();
    }

    @Test
    public void updateOwnerId() throws Exception {
        Long petId = createAndSaveBuddyPet();

        mockMvc.perform(put("/rest/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"ownerId": 2
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name", is("Buddy")),
                jsonPath("$.ownerId", is(2)),
                jsonPath("$.typeId", is(1)),
                jsonPath("$.birthDate", is("2020-04-01"))
        ).andReturn();
    }

    @Test
    public void updateDeleteBirthDate() throws Exception {
        Long petId = createAndSaveBuddyPet();

        mockMvc.perform(put("/rest/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"birthDate": null
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name", is("Buddy")),
                jsonPath("$.ownerId", is(1)),
                jsonPath("$.typeId", is(1)),
                jsonPath("$.birthDate").value(IsNull.nullValue())
        ).andReturn();
    }

    @Test
    public void updateAllProperties() throws Exception {
        Long petId = createAndSaveBuddyPet();

        mockMvc.perform(put("/rest/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Buddy",
                        	"birthDate": "2020-04-01",
                        	"typeId": 2,
                        	"ownerId": 1
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name", is("Buddy")),
                jsonPath("$.ownerId", is(1)),
                jsonPath("$.typeId", is(2)),
                jsonPath("$.birthDate", is("2020-04-01"))
        ).andReturn();
    }

    @Test
    public void bulkOwnerUpdate() throws Exception {
        Long buddyId = createAndSaveBuddyPet();
        Long bellaId = createAndSaveBellaPet();

        mockMvc.perform(put("/rest/pets")
                .param("ids", buddyId.toString(), bellaId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        	"ownerId": 3
                        }""")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$[0]", is(Math.toIntExact(buddyId))),
                jsonPath("$[1]", is(Math.toIntExact(bellaId)))
        ).andReturn();

        checkOwnerUpdated(buddyId, "Buddy", 3L);
        checkOwnerUpdated(bellaId, "Bella", 3L);
    }

    @Test
    public void deleteTest() throws Exception {
        Long petId = createAndSaveBuddyPet();

        mockMvc.perform(delete("/rest/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name", is("Buddy")),
                jsonPath("$.ownerId", is(1)),
                jsonPath("$.typeId", is(1)),
                jsonPath("$.birthDate", is("2020-04-01"))
        ).andReturn();
    }

    @Test
    public void deleteMany() throws Exception {
        Long buddyId = createAndSaveBuddyPet();
        Long bellaId = createAndSaveBellaPet();

        mockMvc.perform(delete("/rest/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .param("ids", buddyId.toString(), bellaId.toString())
        ).andExpectAll(
                status().isOk(),
                jsonPath("$[0]", is(Math.toIntExact(buddyId))),
                jsonPath("$[1]", is(Math.toIntExact(bellaId)))
        ).andReturn();
    }

    private void checkOwnerUpdated(Long petId, String petName, Long newOwnerId) {
        Pet pet = petRepository.findById(petId).orElse(null);
        Assertions.assertNotNull(pet);
        Assertions.assertEquals(petName, pet.getName());
        Assertions.assertEquals(newOwnerId, pet.getOwner().getId());
    }

    private Long createAndSaveBuddyPet() {
        Pet pet = new Pet();
        pet.setName("Buddy");
        pet.setBirthDate(LocalDate.of(2020, 4, 1));
        pet.setType(entityManager.getReference(PetType.class, 1L));
        pet.setOwner(entityManager.getReference(Owner.class, 1L));
        Pet savedPet = petRepository.save(pet);
        Long petId = savedPet.getId();
        afterTestToDeleteIds.add(petId);
        return petId;
    }

    private Long createAndSaveBellaPet() {
        Pet pet = new Pet();
        pet.setName("Bella");
        pet.setBirthDate(LocalDate.of(2020, 4, 1));
        pet.setType(entityManager.getReference(PetType.class, 2L));
        pet.setOwner(entityManager.getReference(Owner.class, 2L));
        Pet savedPet = petRepository.save(pet);
        Long petId = savedPet.getId();
        afterTestToDeleteIds.add(petId);
        return petId;
    }
}
