package com.dmadev.showcase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


//модульные тесты желательно писать как минимум для сервисов, в идеале для всех компонентов
//интеграционными тестами нужно покрывать всё
@ExtendWith(MockitoExtension.class)
class TasksRestControllerTest {
    @Mock
    TaskRepository taskRepository;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    TasksRestController controller;

    //    название теста и ожидаемое его поведение
    @Test
    @DisplayName("GET /api/tasks is return  Http response with status 200 OK and tasks list")
    void handleGetAllTasks_ReturnsValidResponseEntity() {
        //любой тест можно разделить на 3 части
        //дано, вызов, тогда =  given,when,then

        //given
        var user = new ApplicationUser(UUID.randomUUID(), "user1", "password1");

        var tasks = List.of(new Task(UUID.randomUUID(), "First task", false, user.id())
                , new Task(UUID.randomUUID(), "Second task", true, user.id())
        );
        doReturn(tasks).when(this.taskRepository).findByApplicationUserId(user.id());

        //when

        //вызов тестируемого метода
        var responseEntity = this.controller.handleGetAllTasks(user);

        //then

        //проверим что не равен null
        assertNotNull(responseEntity);
        //проверим что равен 200
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        //теперь можно проверить что заголово равен mediaTypeApplicationJSON
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        //Можно проверить что в качестве ответа responseEntity, содержит возвращенный от Репо список задач
        assertEquals(tasks, responseEntity.getBody());
    }


    //в данном случае название тестовых методов будут содержать уже три блока
    @Test
    void handleCreateNewTask_PayloadIsValid_ReturnsValidResponseEntity() {
        //given

        var user = new ApplicationUser(UUID.randomUUID(), "user1", "password1");
        var details = "Third task";

        //when
        //вызов тестируемого метода
        ResponseEntity<?> responseEntity = this.controller.handleCreateNewTask(user, new NewTaskPayload(details),
                UriComponentsBuilder.fromUriString("http://localhost:8080"),
                Locale.ENGLISH
        );
        //then
        assertNotNull(responseEntity);
        //необходимо проверить статус CREATED
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        //теперь проверю что response entity содержит экземпляр класса task
        if (responseEntity.getBody() instanceof Task task) {
            assertNotNull(task.id());
            //class details - соответствует переданному
            assertEquals(details, task.details());
            //а завершенность выполненной задачи имеет значение false
            assertFalse(task.completed());
            assertEquals(user.id(), task.applicationUserId());
            assertEquals(URI.create("http://localhost:8080/api/tasks/" + task.id()),
                    responseEntity.getHeaders().getLocation());

            //нужно проверить что у репо был корректно метод save
            verify(this.taskRepository).save(task);
        } else {
            //тут можно выбросить какое то исключение
            assertInstanceOf(Task.class, responseEntity.getBody());
        }

        //так же проверим что больше не было обращений к репо
        verifyNoMoreInteractions(this.taskRepository);

    }

    @Test
    void handleCreateNewTask_PayloadIsInvalid_ReturnsValidResponseEntity() {
        //given
        var user = new ApplicationUser(UUID.randomUUID(), "user1", "password1");
        var details = "  ";
        var locale = Locale.US;
        var errorMessage = "Details is empty";

        //моделирую поведение метода getMessage у мокОбьекта messageSource
        doReturn(errorMessage).when(this.messageSource)
                .getMessage("tasks.create.details.errors.not_set", new Object[0], locale);

        //when - вызов
        var responseEntity = this.controller.handleCreateNewTask(user, new NewTaskPayload(details),
                UriComponentsBuilder.fromUriString("http://localhost:8080"), locale);

        //then - в итоге , тогда
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        //буду ожидать новый обьект ErrorsPresentation
        assertEquals(new ErrorsPresentation(List.of(errorMessage)), responseEntity.getBody());

        //убедится что у репо не вызывался метод save
        verifyNoInteractions(taskRepository);

    }
}








