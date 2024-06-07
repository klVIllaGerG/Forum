package com.example.integration;

import com.example.controller.NotificationController;
import com.example.entity.vo.response.NotificationVO;
import com.example.service.NotificationService;
import com.example.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(NotificationController.class)
@Import(TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("获取用户通知列表")
    void testListNotification() throws Exception {
        List<NotificationVO> notifications = Arrays.asList(
                new NotificationVO(),
                new NotificationVO()
        );

        when(notificationService.findUserNotification(anyInt())).thenReturn(notifications);

        mockMvc.perform(get("/api/notification/list")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(notifications.size()))
                .andExpect(jsonPath("$.data[0].id").value(notifications.get(0).getId()))
                .andExpect(jsonPath("$.data[1].id").value(notifications.get(1).getId()));
    }

    @Test
    @DisplayName("删除用户通知")
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteUserNotification(anyInt(), anyInt());

        mockMvc.perform(get("/api/notification/delete")
                        .param("id", "1")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除用户所有通知")
    void testDeleteAllNotification() throws Exception {
        doNothing().when(notificationService).deleteUserAllNotification(anyInt());

        mockMvc.perform(get("/api/notification/delete-all")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }
}
