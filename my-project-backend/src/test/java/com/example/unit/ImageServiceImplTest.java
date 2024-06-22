package com.example.unit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Account;
import com.example.entity.dto.StoreImage;
import com.example.mapper.AccountMapper;
import com.example.mapper.ImageStoreMapper;
import com.example.service.impl.ImageServiceImpl;
import com.example.utils.FlowUtils;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageServiceImpl - 图像服务测试")
class ImageServiceImplTest {

    @InjectMocks
    private ImageServiceImpl imageService;

    @Mock
    private MinioClient client;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ImageStoreMapper imageStoreMapper;

    @Mock
    private FlowUtils flowUtils;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        // 设置 baseMapper
        imageService.setImageStoreMapper(imageStoreMapper);
    }

    @Test
    @DisplayName("从 Minio 获取图片 - 成功")
    void testFetchImageFromMinio_Success() {
        OutputStream outputStream = mock(OutputStream.class);
        GetObjectResponse getObjectResponse = new GetObjectResponse(
                null, null, null, "image.jpg", new ByteArrayInputStream("image data".getBytes())
        );

        try {
            when(client.getObject(any())).thenReturn(getObjectResponse);
            imageService.fetchImageFromMinio(outputStream, "image.jpg");
            verify(client).getObject(any());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("上传图片 - 成功")
    void testUploadImage_Success() {
        try {
            when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
            when(imageStoreMapper.insert(any(StoreImage.class))).thenReturn(1);

            String result = imageService.uploadImage(mockFile, 1);

            assertNotNull(result);
            verify(client).putObject(any(PutObjectArgs.class));
            verify(imageStoreMapper).insert(any(StoreImage.class));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("上传图片 - 超过限制")
    void testUploadImage_LimitExceeded() {
        try {
            when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(false);

            String result = imageService.uploadImage(mockFile, 1);

            assertNull(result);
            verify(client, never()).putObject(any(PutObjectArgs.class));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("上传头像 - 成功")
    void testUploadAvatar_Success() {
        try {
            String imageName = UUID.randomUUID().toString().replace("-", "");
            Account account = new Account(1, "username", "password", "email", "USER", "/avatar/oldAvatar", new Date());

            when(accountMapper.selectById(1)).thenReturn(account);
            when(accountMapper.update(any(), any())).thenReturn(1);

            String result = imageService.uploadAvatar(mockFile, 1);

            assertNotNull(result);
            verify(client).putObject(any(PutObjectArgs.class));
            verify(client).removeObject(any(RemoveObjectArgs.class));
            verify(accountMapper).update(any(), any());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("上传头像 - 更新失败")
    void testUploadAvatar_UpdateFailed() {
        try {
            String imageName = UUID.randomUUID().toString().replace("-", "");
            Account account = new Account(1, "username", "password", "email", "USER", "/avatar/oldAvatar", new Date());

            when(accountMapper.selectById(1)).thenReturn(account);
            when(accountMapper.update(any(), any())).thenReturn(0);

            String result = imageService.uploadAvatar(mockFile, 1);

            assertNull(result);
            verify(client).putObject(any(PutObjectArgs.class));
            // 确保在update失败的情况下，不会调用removeObject
            verify(client, never()).removeObject(any(RemoveObjectArgs.class));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
