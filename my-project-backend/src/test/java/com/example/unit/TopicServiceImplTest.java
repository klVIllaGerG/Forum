package com.example.unit;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assets.ZTestReportExtension;
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
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ZTestReportExtension.class})
@DisplayName("TopicServiceImpl - 主题服务测试")
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

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private Topic topic;

    private AccountPrivacy accountPrivacy;


    private AccountDetails accountDetails;
    private TopicComment topicComment;
    private TopicType topicType;
    private Account account;

    @Resource
    StringRedisTemplate template;

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
        account = new Account(2, "testUser", "password", "email@example.com", "USER", "avatarUrl", new Date());

        accountPrivacy = new AccountPrivacy(1);
        accountPrivacy.setPhone(true);
        accountPrivacy.setEmail(true);
        accountPrivacy.setWx(true);
        accountPrivacy.setQq(true);
        accountPrivacy.setGender(true);


        topicService.setTopicMapper(topicMapper);

        accountDetails = new AccountDetails(2, 1, "1234567890", "123456", "wx123456", "User Description");



    }

    @Test
    @DisplayName("列出主题类型 - 成功")
    void testListTypes_Success() {
        when(topicTypeMapper.selectList(any())).thenReturn(Collections.singletonList(topicType));

        List<TopicType> topicTypes = topicService.listTypes();

        assertNotNull(topicTypes);
        assertEquals(1, topicTypes.size());
        verify(topicTypeMapper).selectList(any());
    }

    @Test
    @DisplayName("创建主题 - 成功")
    void testCreateTopic_Success() {
        when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
        when(topicMapper.insert(any(Topic.class))).thenReturn(1);
        when(topicTypeMapper.selectList(any())).thenReturn(Collections.singletonList(topicType));
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
    @DisplayName("更新主题 - 成功")
    void testUpdateTopic_Success() {
        when(topicTypeMapper.selectList(any())).thenReturn(Collections.singletonList(topicType));
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
    @DisplayName("创建评论 - 成功")
    void testCreateComment_Success() {
        when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
        when(topicMapper.selectById(anyInt())).thenReturn(topic);
        when(accountMapper.selectById(anyInt())).thenReturn(account);

        AddCommentVO vo = new AddCommentVO();
        vo.setTid(1);
        vo.setContent("{\"ops\":[{\"insert\":\"Comment Content\"}]}");

        String result = topicService.createComment(2, vo);

        assertNull(result);
        verify(topicCommentMapper).insert(any(TopicComment.class));
        verify(notificationService, atLeastOnce()).addNotification(anyInt(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("删除评论 - 成功")
    void testDeleteComment_Success() {
        when(topicCommentMapper.delete(any(QueryWrapper.class))).thenReturn(1);

        topicService.deleteComment(1, 1);

        verify(topicCommentMapper).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("分页列出主题 - 成功")
    void testGetTopic_Success() {
        when(topicMapper.selectById(anyInt())).thenReturn(topic);
        // 模拟 StringRedisTemplate 的 opsForHash 方法返回 HashOperations

        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(accountMapper.selectById(anyInt())).thenReturn(account);
        when(accountDetailsMapper.selectById(anyInt())).thenReturn(accountDetails) ;
        when(accountPrivacyMapper.selectById(anyInt())).thenReturn(accountPrivacy);
        TopicDetailVO topicDetail = topicService.getTopic(1, 1);

        assertNotNull(topicDetail);
        assertEquals(topic.getTitle(), topicDetail.getTitle());
        verify(topicMapper).selectById(anyInt());

    }

    @Test
    @DisplayName("获取主题详情 - 成功")
    void testInteract_Success() {
        //doReturn(null).when(stringRedisTemplate.opsForHash()).put(anyString(), anyString(), anyString());
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        //doReturn(null).when(stringRedisTemplate).delete(anyString());

        topicService.interact(new Interact(1, 1, new Date(), "like"), true);

        verify(stringRedisTemplate.opsForHash()).put(anyString(), anyString(), anyString());
    }
}
