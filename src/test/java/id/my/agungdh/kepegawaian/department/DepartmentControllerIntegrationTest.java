package id.my.agungdh.kepegawaian.department;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crudFlow() throws Exception {
        String code = uniqueCode("CRUD");

        MvcResult created = mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Departemen Uji","description":"Deskripsi"}
                                """.formatted(code)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.name").value("Departemen Uji"))
                .andReturn();

        String uuid = JsonPath.read(created.getResponse().getContentAsString(), "$.uuid");

        mockMvc.perform(get("/api/departments/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid))
                .andExpect(jsonPath("$.code").value(code));

        mockMvc.perform(put("/api/departments/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Departemen Diubah","description":null}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Departemen Diubah"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        mockMvc.perform(delete("/api/departments/{uuid}", uuid))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/departments/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsDuplicateCodeAndInvalidPayload() throws Exception {
        String code = uniqueCode("DUP");

        create(code, "Departemen Duplikat")
                .andExpect(status().isCreated());

        create(code, "Departemen Duplikat Lagi")
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"","name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void listsWithCursorPaginationAndSort() throws Exception {
        String prefix = "Cursor " + uniqueCode("TEST");
        for (int i = 0; i < 22; i++) {
            create("%s-%02d".formatted(prefix, i), "%s %02d".formatted(prefix, i))
                    .andExpect(status().isCreated());
        }

        MvcResult firstPage = mockMvc.perform(get("/api/departments")
                        .param("name", prefix)
                        .param("sort", "code,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(20))
                .andExpect(jsonPath("$.next").isNotEmpty())
                .andReturn();

        String firstPageBody = firstPage.getResponse().getContentAsString();
        String next = JsonPath.read(firstPageBody, "$.next");
        assertThat(JsonPath.<String>read(firstPageBody, "$.items[0].code")).startsWith(prefix);

        mockMvc.perform(get("/api/departments")
                        .param("name", prefix)
                        .param("sort", "code,asc")
                        .param("after", next))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.next").doesNotExist());
    }

    @Test
    void rejectsInvalidSortAndCursor() throws Exception {
        mockMvc.perform(get("/api/departments").param("sort", "description,asc"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/departments").param("sort", "name,sideways"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/departments").param("after", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/departments").param("after", "bukan-uuid"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions create(String code, String name) throws Exception {
        return mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"code":"%s","name":"%s"}
                        """.formatted(code, name)));
    }

    private String uniqueCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
