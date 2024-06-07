package com.example;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.*;
import com.example.entity.vo.request.AddCommentVO;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.entity.vo.request.TopicUpdateVO;
import com.example.entity.vo.response.CommentVO;
import com.example.entity.vo.response.TopicDetailVO;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.entity.vo.response.TopicTopVO;
import com.example.mapper.*;
import com.example.service.NotificationService;
import com.example.service.impl.TopicServiceImpl;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplTest {

    @InjectMocks
    private TopicServiceImpl topicService;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private TopicTypeMapper topicTypeMapper;

    @Mock
    private FlowUtils flowUtils;

    @Mock
    private CacheUtils cacheUtils;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountDetailsMapper accountDetailsMapper;

    @Mock
    private AccountPrivacyMapper accountPrivacyMapper;

    @Mock
    private TopicCommentMapper topicCommentMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private NotificationService notificationService;

    private Topic topic;
    private TopicComment topicComment;
    private TopicType topicType;
    private Account account;

    @BeforeEach
    void setUp() {
        topic = new Topic();
        topic.setId(1);
        topic.setUid(1);
        topic.setTitle("Test Title");
        topic.setContent("{\"ops\":[{\"insert\":\"Test Content\"}]}");
        topic.setType(1);
        topic.setTime(new Date());

        topicComment = new TopicComment();
        topicComment.setId(1);
        topicComment.setUid(1);
        topicComment.setTid(1);
        topicComment.setContent("{\"ops\":[{\"insert\":\"Test Comment Content\"}]}");
        topicComment.setTime(new Date());
        topicComment.setQuote(0);

        topicType = new TopicType();
        topicType.setId(1);
        topicType.setName("Test Type");

        account = new Account(1, "testUser", "password", "email@example.com", "USER", "avatarUrl", new Date());
    }

    @Test
    @DisplayName("List Topic Types - Success")
    void testListTypes_Success() {
        when(topicTypeMapper.selectList(any())).thenReturn(Collections.singletonList(topicType));

        List<TopicType> topicTypes = topicService.listTypes();

        assertNotNull(topicTypes);
        assertEquals(1, topicTypes.size());
        verify(topicTypeMapper).selectList(any());
    }

    @Test
    @DisplayName("Create Topic - Success")
    void testCreateTopic_Success() {
        when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
        when(topicMapper.insert(any(Topic.class))).thenReturn(1);

        TopicCreateVO vo = new TopicCreateVO();
        vo.setTitle("New Title");
        vo.setContent(JSONObject.parseObject("{\"ops\":[{\"insert\":\"New Content\"}]}"));
        vo.setType(1);

        String result = topicService.createTopic(1, vo);

        assertNull(result);
        verify(topicMapper).insert(any(Topic.class));
        verify(cacheUtils).deleteCachePattern(anyString());
    }

    @Test
    @DisplayName("Update Topic - Success")
    void testUpdateTopic_Success() {
        when(topicMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

        TopicUpdateVO vo = new TopicUpdateVO();
        vo.setId(1);
        vo.setTitle("Updated Title");
        vo.setContent(JSONObject.parseObject("{\"ops\":[{\"insert\":\"Updated Content\"}]}"));
        vo.setType(1);

        String result = topicService.updateTopic(1, vo);

        assertNull(result);
        verify(topicMapper).update(any(), any(UpdateWrapper.class));
    }

    @Test
    @DisplayName("Create Comment - Success")
    void testCreateComment_Success() {
        when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
        when(topicMapper.selectById(anyInt())).thenReturn(topic);
        when(accountMapper.selectById(anyInt())).thenReturn(account);

        AddCommentVO vo = new AddCommentVO();
        vo.setTid(1);
        vo.setContent("{\"ops\":[{\"insert\":\"Comment Content\"}]}");

        String result = topicService.createComment(1, vo);

        assertNull(result);
        verify(topicCommentMapper).insert(any(TopicComment.class));
        verify(notificationService, atLeastOnce()).addNotification(anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Delete Comment - Success")
    void testDeleteComment_Success() {
        when(topicCommentMapper.delete(any(QueryWrapper.class))).thenReturn(1);

        topicService.deleteComment(1, 1);

        verify(topicCommentMapper).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("List Topic By Page - Success")
    void testListTopicByPage_Success() {
        Page<Topic> page = new Page<>();
        page.setRecords(Collections.singletonList(topic));
        when(topicMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        List<TopicPreviewVO> topicPreviews = topicService.listTopicByPage(1, 0);

        assertNotNull(topicPreviews);
        assertEquals(1, topicPreviews.size());
        verify(topicMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("Get Topic - Success")
    void testGetTopic_Success() {
        when(topicMapper.selectById(anyInt())).thenReturn(topic);

        TopicDetailVO topicDetail = topicService.getTopic(1, 1);

        assertNotNull(topicDetail);
        assertEquals(topic.getTitle(), topicDetail.getTitle());
        verify(topicMapper).selectById(anyInt());
    }

    @Test
    @DisplayName("Interact - Success")
    void testInteract_Success() {
        doNothing().when(stringRedisTemplate.opsForHash()).put(anyString(), anyString(), anyString());
        doNothing().when(stringRedisTemplate).delete(anyString());

        topicService.interact(new Interact(1, 1, new Date(), "like"), true);

        verify(stringRedisTemplate.opsForHash()).put(anyString(), anyString(), anyString());
    }
}
