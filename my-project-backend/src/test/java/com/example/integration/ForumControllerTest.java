package com.example.integration;

import com.example.controller.ForumController;
import com.example.entity.vo.request.AddCommentVO;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.entity.vo.request.TopicUpdateVO;
import com.example.entity.vo.response.*;
import com.example.service.TopicService;
import com.example.service.WeatherService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ForumController.class)
@Import(TestConfig.class)
public class ForumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private TopicService topicService;

    @MockBean
    private ControllerUtils utils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        given(utils.messageHandle(Mockito.any())).willReturn(null);
    }

    @Test
    @DisplayName("获取天气信息")
    @WithMockUser
    void testWeather() throws Exception {
        WeatherVO mockWeather = new WeatherVO();
        given(weatherService.fetchWeather(anyDouble(), anyDouble())).willReturn(mockWeather);

        mockMvc.perform(get("/api/forum/weather")
                        .param("longitude", "100.0")
                        .param("latitude", "20.0")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("列出话题")
    @WithMockUser
    void testListTopic() throws Exception {
        List<TopicPreviewVO> topics = Arrays.asList(new TopicPreviewVO(), new TopicPreviewVO());
        given(topicService.listTopicByPage(anyInt(), anyInt())).willReturn(topics);

        mockMvc.perform(get("/api/forum/list-topic")
                        .param("page", "0")
                        .param("type", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("置顶话题")
    @WithMockUser
    void testTopTopic() throws Exception {
        List<TopicTopVO> topics = Arrays.asList(new TopicTopVO(), new TopicTopVO());
        given(topicService.listTopTopics()).willReturn(topics);

        mockMvc.perform(get("/api/forum/top-topic"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取话题详情")
    @WithMockUser
    void testTopic() throws Exception {
        TopicDetailVO topic = new TopicDetailVO();
        given(topicService.getTopic(anyInt(), anyInt())).willReturn(topic);

        mockMvc.perform(get("/api/forum/topic")
                        .param("tid", "1")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("话题互动")
    @WithMockUser
    void testInteract() throws Exception {
        mockMvc.perform(get("/api/forum/interact")
                        .param("tid", "1")
                        .param("type", "like")
                        .param("state", "true")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取收藏列表")
    @WithMockUser
    void testCollects() throws Exception {
        List<TopicPreviewVO> topics = Arrays.asList(new TopicPreviewVO(), new TopicPreviewVO());
        given(topicService.listTopicCollects(anyInt())).willReturn(topics);

        mockMvc.perform(get("/api/forum/collects")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("创建话题")
    @WithMockUser
    void testCreateTopic() throws Exception {
        TopicCreateVO vo = new TopicCreateVO();
        vo.setType(1);
        vo.setTitle("New Topic");
        vo.setContent(com.alibaba.fastjson2.JSONObject.from(new JSONObject()));

        mockMvc.perform(post("/api/forum/create-topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("更新话题")
    @WithMockUser
    void testUpdateTopic() throws Exception {
        TopicUpdateVO vo = new TopicUpdateVO();
        vo.setId(1);
        vo.setType(1);
        vo.setTitle("Updated Topic");
        vo.setContent(com.alibaba.fastjson2.JSONObject.from(new JSONObject()));

        mockMvc.perform(post("/api/forum/update-topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("添加评论")
    @WithMockUser
    void testAddComment() throws Exception {
        AddCommentVO vo = new AddCommentVO();
        vo.setTid(1);
        vo.setContent("This is a comment");
        vo.setQuote(-1);

        mockMvc.perform(post("/api/forum/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取评论列表")
    @WithMockUser
    void testComments() throws Exception {
        List<CommentVO> comments = Arrays.asList(new CommentVO(), new CommentVO());
        given(topicService.comments(anyInt(), anyInt())).willReturn(comments);

        mockMvc.perform(get("/api/forum/comments")
                        .param("tid", "1")
                        .param("page", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除评论")
    @WithMockUser
    void testDeleteComment() throws Exception {
        mockMvc.perform(get("/api/forum/delete-comment")
                        .param("id", "1")
                        .requestAttr(Const.ATTR_USER_ID, 1))
                .andExpect(status().isOk());
    }
}
