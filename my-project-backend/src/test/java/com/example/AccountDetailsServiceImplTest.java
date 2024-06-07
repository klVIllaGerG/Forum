package com.example;

import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.vo.request.DetailsSaveVO;
import com.example.mapper.AccountDetailsMapper;
import com.example.service.AccountService;
import com.example.service.impl.AccountDetailsServiceImpl;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AccountDetailsServiceImplTest {

    @InjectMocks
    private AccountDetailsServiceImpl accountDetailsService;

    @MockBean
    private AccountDetailsMapper accountDetailsMapper;

    @MockBean
    private AccountService accountService;

    private AccountDetails accountDetails;
    private DetailsSaveVO detailsSaveVO;
    private Account account;

    @BeforeEach
    void setUp() {
        accountDetails = new AccountDetails(1, 1, "1234567890", "123456", "wx123", "Description");
        detailsSaveVO = new DetailsSaveVO();
        detailsSaveVO.setUsername("testuser");
        detailsSaveVO.setGender(1);
        detailsSaveVO.setPhone("1234567890");
        detailsSaveVO.setQq("123456");
        detailsSaveVO.setWx("wx123");
        detailsSaveVO.setDesc("Description");

        account = new Account(1, "testuser", "password", "test@example.com", "USER", "avatar.png", new Date());
    }

    @Test
    @DisplayName("Find Account Details By ID")
    void testFindAccountDetailsById() {
        when(accountDetailsMapper.selectById(1)).thenReturn(accountDetails);

        AccountDetails result = accountDetailsService.findAccountDetailsById(1);

        assertNotNull(result);
        assertEquals(accountDetails, result);
    }

    @Test
    @DisplayName("Save Account Details - Success")
    void testSaveAccountDetails_Success() {
        UpdateChainWrapper<Account> updateWrapper = mock(UpdateChainWrapper.class);

        when(accountService.findAccountByNameOrEmail(detailsSaveVO.getUsername())).thenReturn(null);
        when(accountService.update()).thenReturn(updateWrapper);
        when(updateWrapper.eq("id", 1)).thenReturn(updateWrapper);
        when(updateWrapper.set("username", detailsSaveVO.getUsername())).thenReturn(updateWrapper);
        when(updateWrapper.update()).thenReturn(true);
        when(accountDetailsMapper.insert(any(AccountDetails.class))).thenReturn(1);
        when(accountDetailsMapper.updateById(any(AccountDetails.class))).thenReturn(1);
        when(accountDetailsMapper.selectById(1)).thenReturn(accountDetails);

        boolean result = accountDetailsService.saveAccountDetails(1, detailsSaveVO);

        assertTrue(result);
        verify(accountService.update(), times(1)).eq("id", 1).set("username", detailsSaveVO.getUsername()).update();
        verify(accountDetailsMapper, times(1)).insert(any(AccountDetails.class));
        verify(accountDetailsMapper, times(1)).updateById(any(AccountDetails.class));
    }

    @Test
    @DisplayName("Save Account Details - Account Exists with Different ID")
    void testSaveAccountDetails_AccountExistsWithDifferentID() {
        Account otherAccount = new Account(2, "otheruser", "password", "other@example.com", "USER", "avatar.png", new Date());
        when(accountService.findAccountByNameOrEmail(detailsSaveVO.getUsername())).thenReturn(otherAccount);

        boolean result = accountDetailsService.saveAccountDetails(1, detailsSaveVO);

        assertFalse(result);
        verify(accountService, never()).update();
        verify(accountDetailsMapper, never()).insert(any(AccountDetails.class));
    }
}
