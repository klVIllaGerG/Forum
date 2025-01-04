package com.example.unit;

import com.example.assets.ZTestReportExtension;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ZTestReportExtension.class})
@DisplayName("WeatherServiceImpl - 天气服务测试")
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
        byte[] compressedData = compressStringToGzip("{\"location\":{\"id\":123,\"name\":\"测试城市\"},\"now\":{\"temp\":\"22\",\"weather\":\"晴朗\"},\"hourly\":[{\"hour\":\"1\",\"temp\":\"20\"}]}");
        when(restTemplate.getForObject(anyString(), eq(byte[].class))).thenReturn(compressedData);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // 假设缓存未命中
    }

    @Test
    @DisplayName("通过 API 调用获取天气信息测试")
    void testFetchWeatherWithApiCall() {
        // 默认设置
        double longitude = 30.267153;
        double latitude = -97.7430607;
        WeatherVO result = weatherService.fetchWeather(longitude, latitude);

        assertNotNull(result);
        assertEquals("测试城市", result.getLocation().getString("name"));
        verify(restTemplate, times(3)).getForObject(anyString(), eq(byte[].class)); // 分别调用获取地点、当前天气和每小时天气
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any()); // 确保设置缓存
    }

    // 辅助方法：为模拟的 RestTemplate 压缩 JSON 字符串
    private byte[] compressStringToGzip(String input) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(input.getBytes());
        } catch (IOException e) {
            fail("创建测试用的压缩数据失败。");
        }
        return byteArrayOutputStream.toByteArray();
    }
}