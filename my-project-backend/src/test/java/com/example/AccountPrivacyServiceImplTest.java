package com.example;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.request.PrivacySaveVO;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.impl.AccountPrivacyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DisplayName("Account Privacy Service Tests")
class AccountPrivacyServiceImplTest {

    @InjectMocks
    private AccountPrivacyServiceImpl accountPrivacyService;

    @MockBean
    private AccountPrivacyMapper accountPrivacyMapper;

    private AccountPrivacy accountPrivacy;
    private PrivacySaveVO privacySaveVO;

    @BeforeEach
    void setUp() {
        accountPrivacy = new AccountPrivacy(1);
        privacySaveVO = new PrivacySaveVO();
        privacySaveVO.setType("phone");
        privacySaveVO.setStatus(false);
    }

    @Test
    @DisplayName("Save Privacy - Existing Privacy")
    void testSavePrivacy_ExistingPrivacy() {
        when(accountPrivacyMapper.selectById(1)).thenReturn(accountPrivacy);

        accountPrivacyService.savePrivacy(1, privacySaveVO);

        verify(accountPrivacyMapper).selectById(1);
        verify(accountPrivacyMapper).updateById(any(AccountPrivacy.class));
    }

    @Test
    @DisplayName("Save Privacy - New Privacy")
    void testSavePrivacy_NewPrivacy() {
        when(accountPrivacyMapper.selectById(1)).thenReturn(null);

        accountPrivacyService.savePrivacy(1, privacySaveVO);

        verify(accountPrivacyMapper).selectById(1);
        verify(accountPrivacyMapper).insert(any(AccountPrivacy.class));
    }

    @Test
    @DisplayName("Get Account Privacy - Existing Privacy")
    void testAccountPrivacy_ExistingPrivacy() {
        when(accountPrivacyMapper.selectById(1)).thenReturn(accountPrivacy);

        AccountPrivacy result = accountPrivacyService.accountPrivacy(1);

        assertNotNull(result);
        assertEquals(accountPrivacy, result);
    }

    @Test
    @DisplayName("Get Account Privacy - New Privacy")
    void testAccountPrivacy_NewPrivacy() {
        when(accountPrivacyMapper.selectById(1)).thenReturn(null);

        AccountPrivacy result = accountPrivacyService.accountPrivacy(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }
}
