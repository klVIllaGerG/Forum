package com.example.unit;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.assets.ZTestReportExtension;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.impl.AccountServiceImpl;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@ExtendWith({MockitoExtension.class, ZTestReportExtension.class})
@DisplayName("AccountServiceImpl - 账户服务测试")
class AccountServiceImplTest {

    @Autowired
    private AccountServiceImpl accountService;

    @MockBean
    private AccountMapper accountMapper;

    @MockBean
    private AccountDetailsMapper detailsMapper;

    @MockBean
    private AccountPrivacyMapper privacyMapper;

    @MockBean
    private AmqpTemplate rabbitTemplate;

    @MockBean
    private RabbitTemplate rabbitMessagingTemplate; // 添加这一行来模拟 RabbitTemplate

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private FlowUtils flow;

    @BeforeEach
    void setUp() {
        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("通过用户名加载用户 - 用户存在")
    void testLoadUserByUsername_UserFound() {
        String username = "testuser";
        Account mockAccount = new Account(1, username, "password", "test@example.com", "USER", null, new Date());

        Mockito.when(accountMapper.selectOne(any())).thenReturn(mockAccount);

        UserDetails userDetails = accountService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("通过用户名加载用户 - 用户不存在")
    void testLoadUserByUsername_UserNotFound() {
        String username = "nonexistentuser";

        Mockito.when(accountMapper.selectOne(any())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            accountService.loadUserByUsername(username);
        });
    }

    @Test
    @DisplayName("注册电子邮件账户 - 成功")
    void testRegisterEmailAccount_Success() {
        EmailRegisterVO info = new EmailRegisterVO();
        info.setEmail("test@example.com");
        info.setUsername("testuser");
        info.setPassword("password");
        info.setCode("123456");

        Mockito.when(valueOperations.get(Const.VERIFY_EMAIL_DATA + info.getEmail())).thenReturn("123456");
        Mockito.when(passwordEncoder.encode(info.getPassword())).thenReturn("encodedPassword");
        Mockito.when(accountMapper.insert(any(Account.class))).thenReturn(1);
        Mockito.when(privacyMapper.insert(any(AccountPrivacy.class))).thenReturn(1);
        Mockito.when(detailsMapper.insert(any(AccountDetails.class))).thenReturn(1);
        Mockito.when(accountMapper.exists(any())).thenReturn(false);

        String result = accountService.registerEmailAccount(info);

        assertNull(result);
    }

    @Test
    @DisplayName("注册电子邮件账户 - 邮箱已注册")
    void testRegisterEmailAccount_EmailAlreadyRegistered() {
        EmailRegisterVO info = new EmailRegisterVO();
        info.setEmail("test@example.com");
        info.setUsername("testuser");
        info.setPassword("password");
        info.setCode("123456");

        Mockito.when(valueOperations.get(Const.VERIFY_EMAIL_DATA + info.getEmail())).thenReturn("123456");
        Mockito.when(accountMapper.exists(Wrappers.<Account>query().eq("email", info.getEmail()))).thenReturn(true);

        String result = accountService.registerEmailAccount(info);

        assertEquals("内部错误，注册失败", result);
    }

    @Test
    @DisplayName("注册电子邮件账户 - 用户名已被占用")
    void testRegisterEmailAccount_UsernameAlreadyTaken() {
        EmailRegisterVO info = new EmailRegisterVO();
        info.setEmail("test@example.com");
        info.setUsername("testuser");
        info.setPassword("password");
        info.setCode("123456");

        Mockito.when(valueOperations.get(Const.VERIFY_EMAIL_DATA + info.getEmail())).thenReturn("123456");
        Mockito.when(accountMapper.exists(Wrappers.<Account>query().eq("email", info.getEmail()))).thenReturn(false);
        Mockito.when(accountMapper.exists(Wrappers.<Account>query().eq("username", info.getUsername()))).thenReturn(true);

        String result = accountService.registerEmailAccount(info);

        assertEquals("内部错误，注册失败", result);
    }

    @Test
    @DisplayName("注册电子邮件账户 - 验证码无效")
    void testRegisterEmailAccount_InvalidCode() {
        EmailRegisterVO info = new EmailRegisterVO();
        info.setEmail("test@example.com");
        info.setUsername("testuser");
        info.setPassword("password");
        info.setCode("654321");

        Mockito.when(valueOperations.get(Const.VERIFY_EMAIL_DATA + info.getEmail())).thenReturn("123456");

        String result = accountService.registerEmailAccount(info);

        assertEquals("验证码错误，请重新输入", result);
    }

    @Test
    @DisplayName("注册电子邮件账户 - 缺少验证码")
    void testRegisterEmailAccount_MissingCode() {
        EmailRegisterVO info = new EmailRegisterVO();
        info.setEmail("test@example.com");
        info.setUsername("testuser");
        info.setPassword("password");
        info.setCode(null);

        Mockito.when(valueOperations.get(Const.VERIFY_EMAIL_DATA + info.getEmail())).thenReturn(null);

        String result = accountService.registerEmailAccount(info);

        assertEquals("请先获取验证码", result);
    }

    @Test
    @DisplayName("生成注册电子邮件验证码 - 成功")
    void testRegisterEmailVerifyCode_Success() {
        String type = "register";
        String email = "test@example.com";
        String address = "127.0.0.1";

        Mockito.when(flow.limitOnceCheck(any(), anyInt())).thenReturn(true);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(any(String.class), any(Map.class));
        Mockito.doNothing().when(valueOperations).set(any(String.class), any(String.class), anyLong(), any(TimeUnit.class));

        String result = accountService.registerEmailVerifyCode(type, email, address);

        assertNull(result);
    }

    @Test
    @DisplayName("生成注册电子邮件验证码 - 请求过于频繁")
    void testRegisterEmailVerifyCode_RequestLimit() {
        String type = "register";
        String email = "test@example.com";
        String address = "127.0.0.1";

        Mockito.when(flow.limitOnceCheck(any(), anyInt())).thenReturn(false);

        String result = accountService.registerEmailVerifyCode(type, email, address);

        assertEquals("请求频繁，请稍后再试", result);
    }
}
