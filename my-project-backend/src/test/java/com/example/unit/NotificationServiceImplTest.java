package com.example.unit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.assets.ZTestReportExtension;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith({MockitoExtension.class, ZTestReportExtension.class})
@DisplayName("NotificationServiceImpl - 通知服务测试")
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService.setNotificationMapper(notificationMapper);
    }

    @Test
    @DisplayName("添加通知")
    void testAddNotification() {
        Notification mockNotification = new Notification();
        mockNotification.setUid(1);
        mockNotification.setTitle("Maintenance");
        mockNotification.setContent("System will be down");
        mockNotification.setType("Alert");
        mockNotification.setUrl("http://example.com/maintenance");
        notificationService.addNotification(1, "Maintenance", "System will be down", "Alert", "http://example.com/maintenance");

        verify(notificationMapper).insert(eq(mockNotification));
    }

    @Test
    @DisplayName("查找用户通知")
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
    @DisplayName("删除用户通知")
    void testDeleteUserNotification() {
        notificationService.deleteUserNotification(1, 1);
        verify(notificationMapper).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("删除用户所有通知")
    void testDeleteUserAllNotification() {
        notificationService.deleteUserAllNotification(1);
        verify(notificationMapper).delete(any(QueryWrapper.class));
    }
}
