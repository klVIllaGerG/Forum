package com.example;

import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.impl.WeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        // 使用有效的压缩数据进行模拟
        byte[] compressedData = compressStringToGzip("{\"location\":{\"id\":123,\"name\":\"Test City\"},\"now\":{\"temp\":\"22\",\"weather\":\"Sunny\"},\"hourly\":[{\"hour\":\"1\",\"temp\":\"20\"}]}");
        when(restTemplate.getForObject(anyString(), eq(byte[].class))).thenReturn(compressedData);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Assume cache miss
    }

    @Test
    @DisplayName("Test Fetch Weather with API Call")
    void testFetchWeatherWithApiCall() {
        double longitude = 30.267153;
        double latitude = -97.7430607;
        WeatherVO result = weatherService.fetchWeather(longitude, latitude);

        assertNotNull(result);
        assertEquals("Test City", result.getLocation().getString("name"));
        verify(restTemplate, times(3)).getForObject(anyString(), eq(byte[].class)); // Called for location, now, and hourly
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any()); // Ensure cache is set
    }

    // Helper method to compress a JSON string for the mock RestTemplate
    private byte[] compressStringToGzip(String input) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(input.getBytes());
        } catch (IOException e) {
            fail("Failed to create compressed data for testing.");
        }
        return byteArrayOutputStream.toByteArray();
    }
}
