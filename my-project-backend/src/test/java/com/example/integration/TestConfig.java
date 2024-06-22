package com.example.integration;

import com.example.service.ImageService;
import com.example.utils.FlowUtils;
import com.example.utils.JwtUtils;
import com.example.utils.SnowflakeIdGenerator;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
// @Profile("com.example.integration")  // Ensure this configuration is only loaded in the test profile
public class TestConfig {

    @Bean
    @Primary
    public FlowUtils testFlowUtils() {
        FlowUtils mockFlowUtils = Mockito.mock(FlowUtils.class);
        Mockito.when(mockFlowUtils.limitOnceUpgradeCheck(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockFlowUtils.limitOnceCheck(Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockFlowUtils.limitPeriodCheck(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockFlowUtils.limitPeriodCounterCheck(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        return mockFlowUtils;
    }

    @Bean
    @Primary
    public SnowflakeIdGenerator testsnowflakeIdGenerator() {
        return Mockito.mock(SnowflakeIdGenerator.class);
    }

    @Bean
    @Primary
    public StringRedisTemplate teststringRedisTemplate() {
        return Mockito.mock(StringRedisTemplate.class);
    }

    @Bean
    @Primary
    public JwtUtils testjwtUtils() {
        return Mockito.mock(JwtUtils.class);
    }

    @Bean
    @Primary
    public ImageService testimageService() {
        return Mockito.mock(ImageService.class);
    }

}
