package com.example;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.Notification;
import com.example.entity.vo.response.NotificationVO;
import com.example.mapper.NotificationMapper;
import com.example.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        // Mocking the NotificationMapper to return expected values
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.delete(any(QueryWrapper.class))).thenReturn(1);

        // Make sure we return a valid mapper from the mocked service
        Mockito.when(notificationService.getBaseMapper()).thenReturn(notificationMapper);
    }

    @Test
    @DisplayName("Add Notification")
    void testAddNotification() {
        notificationService.addNotification(1, "Maintenance", "System will be down", "Alert", "http://example.com/maintenance");

        verify(notificationMapper).insert(any(Notification.class));
    }

    @Test
    @DisplayName("Find User Notifications")
    void testFindUserNotification() {
        List<Notification> mockList = new ArrayList<>();
        Notification notification = new Notification();
        notification.setId(1);
        notification.setUid(1);
        notification.setTitle("New Update");
        notification.setContent("Check out the latest update!");
        notification.setType("Update");
        notification.setUrl("http://example.com/update");
        notification.setTime(new Date());
        mockList.add(notification);

        when(notificationMapper.selectList(any(QueryWrapper.class))).thenReturn(mockList);

        List<NotificationVO> results = notificationService.findUserNotification(1);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("New Update", results.get(0).getTitle());

        verify(notificationMapper).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("Delete User Notification")
    void testDeleteUserNotification() {
        notificationService.deleteUserNotification(1, 1);
        verify(notificationMapper).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("Delete All User Notifications")
    void testDeleteUserAllNotification() {
        notificationService.deleteUserAllNotification(1);
        verify(notificationMapper).delete(any(QueryWrapper.class));
    }
}
