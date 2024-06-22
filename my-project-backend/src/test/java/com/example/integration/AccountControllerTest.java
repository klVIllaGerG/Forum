package com.example.integration;

import com.example.controller.AccountController;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.request.ChangePasswordVO;
import com.example.entity.vo.request.DetailsSaveVO;
import com.example.entity.vo.request.ModifyEmailVO;
import com.example.entity.vo.request.PrivacySaveVO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import com.example.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(AccountController.class)
@Import(TestConfig.class)
@DisplayName("AccountController - 用户控制类测试")
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    @MockBean
    private AccountPrivacyService accountPrivacyService;

    @MockBean
    private ControllerUtils controllerUtils;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private final int userId = 1;
    private String jwtToken = "test-jwt-token";

    private Account account;
    private AccountDetails accountDetails;
    private AccountPrivacy accountPrivacy;

    @BeforeEach
    void setUp() {
        account = new Account(userId, "john.doe", "password", "john.doe@example.com", "USER", "avatar.png", new Date());
        accountDetails = new AccountDetails(userId, 0, "1234567890", "123456", "userwx", "desc");
        accountPrivacy = new AccountPrivacy(userId);
        accountPrivacy.setPhone(false);  // 确保电话隐私设置为false
    }

    @Test
    @DisplayName("获取用户信息")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testGetAccountInfo() throws Exception {
        given(accountService.findAccountById(userId)).willReturn(account);

        mockMvc.perform(get("/api/user/info")
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john.doe"));
    }

    @Test
    @DisplayName("获取用户详细信息")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testGetAccountDetails() throws Exception {
        given(accountDetailsService.findAccountDetailsById(userId)).willReturn(accountDetails);

        mockMvc.perform(get("/api/user/details")
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("1234567890"));
    }

    @Test
    @DisplayName("保存用户详细信息")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testSaveAccountDetails() throws Exception {
        DetailsSaveVO detailsSaveVO = new DetailsSaveVO();
        detailsSaveVO.setUsername("newuser");  // 确保用户名在1到10个字符之间
        detailsSaveVO.setGender(0);
        detailsSaveVO.setPhone("1234567890");
        detailsSaveVO.setQq("123456");
        detailsSaveVO.setWx("userwx");
        detailsSaveVO.setDesc("New description");

        given(accountDetailsService.saveAccountDetails(userId, detailsSaveVO)).willReturn(true);

        mockMvc.perform(post("/api/user/save-details")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Adding CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detailsSaveVO))
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("修改邮箱")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testModifyEmail() throws Exception {
        ModifyEmailVO modifyEmailVO = new ModifyEmailVO();
        modifyEmailVO.setEmail("new.email@example.com");
        modifyEmailVO.setCode("123456");

        given(controllerUtils.messageHandle(any())).willReturn(RestBean.success());

        mockMvc.perform(post("/api/user/modify-email")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Adding CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyEmailVO))
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("修改密码")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testChangePassword() throws Exception {
        ChangePasswordVO changePasswordVO = new ChangePasswordVO();
        changePasswordVO.setPassword("oldPassword");
        changePasswordVO.setNew_password("newPassword");

        given(controllerUtils.messageHandle(any())).willReturn(RestBean.success());

        mockMvc.perform(post("/api/user/change-password")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Adding CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordVO))
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("保存隐私信息")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testSavePrivacySettings() throws Exception {
        PrivacySaveVO privacySaveVO = new PrivacySaveVO();
        privacySaveVO.setType("phone");
        privacySaveVO.setStatus(false);

        mockMvc.perform(post("/api/user/save-privacy")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // Adding CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(privacySaveVO))
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取隐私信息")
    @WithMockUser(username = "john.doe", roles = {"USER"})
    void testGetPrivacySettings() throws Exception {
        given(accountPrivacyService.accountPrivacy(userId)).willReturn(accountPrivacy);

        mockMvc.perform(get("/api/user/privacy")
                        .requestAttr(Const.ATTR_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value(false));  // 确认返回值为false
    }
}
