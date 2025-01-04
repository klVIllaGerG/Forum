package com.example.integration;

import com.example.assets.ZTestReportExtension;
import com.example.controller.ImageController;
import com.example.service.ImageService;
import com.example.utils.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
@ExtendWith({ZTestReportExtension.class})
@ContextConfiguration(classes = {ImageControllerTest.TestConfig.class, ImageController.class})
@DisplayName("ImageController - 图像控制类测试")
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Configuration
    static class TestConfig {

        @Bean
        @Primary
        public StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }
    }

    @Test
    @DisplayName("上传图片成功")
    @WithMockUser
    void uploadImageSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test image content".getBytes());

        given(imageService.uploadImage(any(), anyInt())).willReturn("http://example.com/image.jpg");

        mockMvc.perform(multipart("/api/image/cache")
                        .file(file)
                        .requestAttr(Const.ATTR_USER_ID, 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("http://example.com/image.jpg"));
    }

    @Test
    @DisplayName("上传头像成功")
    @WithMockUser
    void uploadAvatarSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "test avatar content".getBytes());

        given(imageService.uploadAvatar(any(), anyInt())).willReturn("http://example.com/avatar.jpg");

        mockMvc.perform(multipart("/api/image/avatar")
                        .file(file)
                        .requestAttr(Const.ATTR_USER_ID, 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("http://example.com/avatar.jpg"));
    }

    @Test
    @DisplayName("上传图片失败，文件太大")
    @WithMockUser
    void uploadImageFailureDueToSize() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1024 * 1024 * 6]);

        mockMvc.perform(multipart("/api/image/cache")
                        .file(file)
                        .requestAttr(Const.ATTR_USER_ID, 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("头像图片不能大于5MB"));
    }

    @Test
    @DisplayName("上传头像失败，文件太大")
    @WithMockUser
    void uploadAvatarFailureDueToSize() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[1024 * 101]);

        mockMvc.perform(multipart("/api/image/avatar")
                        .file(file)
                        .requestAttr(Const.ATTR_USER_ID, 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("头像图片不能大于100KB"));
    }

    @Test
    @DisplayName("上传图片失败，服务错误")
    @WithMockUser
    void uploadImageServiceFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test image content".getBytes());

        given(imageService.uploadImage(any(), anyInt())).willReturn(null);

        mockMvc.perform(multipart("/api/image/cache")
                        .file(file)
                        .requestAttr(Const.ATTR_USER_ID, 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("图片上传失败，请联系管理员！"));
    }

    @Test
    @DisplayName("上传头像失败，服务错误")
    @WithMockUser
    void uploadAvatarServiceFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "test avatar content".getBytes());

        given(imageService.uploadAvatar(any(), anyInt())).willReturn(null);

        mockMvc.perform(multipart("/api/image/avatar")
                        .file(file)
                        .requestAttr(Const.ATTR_USER_ID, 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("头像上传失败，请联系管理员！"));
    }
}
