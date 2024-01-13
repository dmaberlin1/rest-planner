package com.dmadev.showcase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//IT integration test
//для интеграционного теста потребуется приложение spring boot, которое +- выглядит как реально,
// необходимо чтобы приложение развертывалось перед тестированием
// для этого есть аннотация @SpringBootTest  () - можно указать параметры для доп конфигурации окружения
//для тест endpointoв потребуется mockmvc  - это @AutoConfigureMockMvc
//printOnlyOnFailure = false - для того чтобы в логах смотреть ответы на запросы и ответы
@Sql("/sql/tasks_rest_controller/test_data.sql")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TasksRestControllerIT {

    @Autowired
    MockMvc mockMvc;

//    @Autowired
//    InMemTaskRepository taskRepository;

    //тесты не должны опиратся на результаты других тестов, соотвественно после выполненпия нужно удалять результаты
    // в случае с реляционными бд , я делаю автооткат при помощи аннотации Transactional на уровне класса или тестового метода
    //в моём случае существующие задачи
//    @AfterEach
//    void tearDown() {
//        this.taskRepository.getTasks().clear();
//    }


    @Test
//    @WithMockUser
    void handleGetAllTasks_ReturnsValidResponseEntity() throws Exception {
        //given
        MockHttpServletRequestBuilder requestBuilder = get("/api/tasks")
                .with(httpBasic("user1","password1"));
//        this.taskRepository.getTasks().addAll(List.of(
//                new Task(UUID.fromString("cb8e1f2c-b140-11ee-bfa0-2bfffbc562af"), "First task", false),
//                new Task(UUID.fromString("d8a71876-b140-11ee-a438-c349ef386ab1"), "Second task", true)
//        ));

        //when
        this.mockMvc.perform(requestBuilder)

                //then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                {
                                "id": "cb8e1f2c-b140-11ee-bfa0-2bfffbc562af",
                                "details": "First task",
                                "completed": false
                                },{
                                "id": "d8a71876-b140-11ee-a438-c349ef386ab1",
                                "details": "Second task",
                                "completed": true
                                }
                                ]
                                """));
    }

    @Test
    void handleCreateNewTask_PayloadIsValid_ReturnsValidResponseEntity() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/tasks")

                .with(httpBasic("user2","password2"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "details": "Third task"
                        }
                        """);

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        header().exists(HttpHeaders.LOCATION),
                        content().json("""
                                {
                                "details": "Third task",
                                "completed": false
                                }
                                """),
                        jsonPath("$.id").exists()
                );
//
//        assertEquals(1,this.taskRepository.getTasks().size());
//        UUID task = this.taskRepository.getTasks().get(0).id();
//        assertNotNull(task);
//        assertEquals("Third task",this.taskRepository.getTasks().get(0).details());
//        assertFalse(this.taskRepository.getTasks().get(0).completed());
    }

    @Test
    void handleCreateNewTask_PayloadIsInvalid_ReturnsValidResponseEntity() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/tasks")
                .with(httpBasic("user","demoPassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE,"en")
                .content("""
                        {
                            "details": null
                        }
                        """);

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().json("""
                                {
                                "errors": ["Task details must be set"]
                                }
                                """,true)
                );

//        assertTrue(this.taskRepository.getTasks().isEmpty());
    }
}