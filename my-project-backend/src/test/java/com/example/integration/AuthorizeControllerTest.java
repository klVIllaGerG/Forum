package com.example.integration;

import com.example.controller.AuthorizeController;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.service.AccountService;
import com.example.utils.ControllerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthorizeController.class)
@Import(TestConfig.class)
@DisplayName("AuthorizeController - 授权控制类测试")
public class AuthorizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ControllerUtils controllerUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        given(controllerUtils.messageHandle(Mockito.any())).willReturn(null);  // Adjust this line as needed.
    }

    @Test
    @DisplayName("请求邮件验证码")
    @WithMockUser // 这将模拟一个认证用户的存在
    void testAskVerifyCode() throws Exception {
        mockMvc.perform(get("/api/auth/ask-code")
                        .param("email", "test@example.com")
                        .param("type", "register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("用户注册")
    @WithMockUser
    void testRegister() throws Exception {
        EmailRegisterVO vo = new EmailRegisterVO();
        vo.setEmail("test@example.com");
        vo.setPassword("password123");
        vo.setCode("123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("密码重置确认")
    @WithMockUser
    void testResetConfirm() throws Exception {
        ConfirmResetVO vo = new ConfirmResetVO("test@example.com", "123456");

        mockMvc.perform(post("/api/auth/reset-confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("密码重置操作")
    @WithMockUser
    void testResetPassword() throws Exception {
        EmailResetVO vo = new EmailResetVO();
        vo.setEmail("test@example.com");
        vo.setCode("123456");
        vo.setPassword("newPass123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }
}
