package com.example.integration;

import com.example.service.ImageService;
import com.example.utils.FlowUtils;
import com.example.utils.JwtUtils;
import com.example.utils.SnowflakeIdGenerator;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public FlowUtils flowUtils() {
        FlowUtils mockFlowUtils = Mockito.mock(FlowUtils.class);
        Mockito.when(mockFlowUtils.limitOnceUpgradeCheck(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockFlowUtils.limitOnceCheck(Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockFlowUtils.limitPeriodCheck(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockFlowUtils.limitPeriodCounterCheck(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        return mockFlowUtils;
    }

    @Bean
    @Primary
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return Mockito.mock(SnowflakeIdGenerator.class);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return Mockito.mock(StringRedisTemplate.class);
    }

    @Bean
    public JwtUtils jwtUtils() {
        return Mockito.mock(JwtUtils.class);
    }

    @Bean
    public ImageService imageService() {
        return Mockito.mock(ImageService.class);
    }

}
