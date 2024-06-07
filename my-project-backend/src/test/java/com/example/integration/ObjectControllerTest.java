package com.example.integration;

import com.example.controller.ObjectController;
import com.example.service.ImageService;
import com.example.entity.RestBean;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.ServletOutputStream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ObjectController.class)
public class ObjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @BeforeEach
    public void setUp() {
        // 初始化Mock对象
    }

    @Test
    public void testImageFetch_NotFound() throws Exception {
        String imagePath = "/images/shortpath";
        mockMvc.perform(get(imagePath))
                .andExpect(status().isNotFound())
                .andExpect(content().string(RestBean.failure(404, "Not found").toString()));
    }

    @Test
    public void testImageFetch_ValidPath() throws Exception {
        String imagePath = "/images/validpath";

        doAnswer(invocation -> {
            ServletOutputStream stream = invocation.getArgument(0);
            stream.print("image data");
            return null;
        }).when(imageService).fetchImageFromMinio(any(ServletOutputStream.class), eq("validpath"));

        mockMvc.perform(get(imagePath))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=2592000"))
                .andExpect(header().string("Content-Type", "image/jpg"));
    }

    @Test
    public void testImageFetch_MinioNotFound() throws Exception {
        String imagePath = "/images/notfound";

        ErrorResponse errorResponse = new ErrorResponse();
        ErrorResponseException exception = new ErrorResponseException(errorResponse, null, null);

        doThrow(exception).when(imageService).fetchImageFromMinio(any(ServletOutputStream.class), eq("notfound"));

        mockMvc.perform(get(imagePath))
                .andExpect(status().isNotFound())
                .andExpect(content().string(RestBean.failure(404, "Not found").toString()));
    }
}
