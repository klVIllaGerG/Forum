package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.WeatherService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    RestTemplate rest;

    @Resource
    StringRedisTemplate template;

    @Value("${spring.weather.key}")
    String key;

    public WeatherVO fetchWeather(double longitude, double latitude){
        return fetchFromCache(longitude, latitude);
    }

    private WeatherVO fetchFromCache(double longitude, double latitude){
        System.out.println("fetchLocation="+longitude+latitude);
        JSONObject geo = this.decompressStingToJson(rest.getForObject(
                "https://geoapi.qweather.com/v2/city/lookup?location="+longitude+","+latitude+"&key="+key, byte[].class));
        System.out.println("fetchLocation="+geo);
        if(geo == null) return null;

        JSONArray locations = geo.getJSONArray("location");
        if (locations == null || locations.isEmpty()) {
            return null; // 无位置信息返回null
        }
        JSONObject location = locations.getJSONObject(0);

        int id = location.getInteger("id");
        String key = Const.FORUM_WEATHER_CACHE + id;
        String cache = template.opsForValue().get(key);
        if(cache != null) {
            return JSONObject.parseObject(cache).to(WeatherVO.class);
        }
        WeatherVO vo = this.fetchFromAPI(id, location);
        if(vo == null) return null;

        template.opsForValue().set(key, JSONObject.from(vo).toJSONString(), 1, TimeUnit.HOURS);
        return vo;
    }

    private WeatherVO fetchFromAPI(int id, JSONObject location){
        WeatherVO vo = new WeatherVO();
        vo.setLocation(location);

        JSONObject now = this.decompressStingToJson(rest.getForObject(
                "https://devapi.qweather.com/v7/weather/now?location="+id+"&key="+key, byte[].class));
        if(now == null) return null;
        vo.setNow(now.getJSONObject("now"));

        JSONObject hourly = this.decompressStingToJson(rest.getForObject(
                "https://devapi.qweather.com/v7/weather/24h?location="+id+"&key="+key, byte[].class));
        if(hourly == null) return null;

        JSONArray hourlyArray = hourly.getJSONArray("hourly");
        if (hourlyArray == null) {
            return null; // 无小时数据返回null
        }
        vo.setHourly(new JSONArray(hourlyArray.stream().limit(5).toList()));
        return vo;
    }

    public JSONObject decompressStingToJson(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = gzip.read(buffer)) != -1) {
                stream.write(buffer, 0, read);
            }
            return JSONObject.parseObject(stream.toString());
        } catch (IOException e) {
            System.out.println("Failed to decompress and parse JSON: " + e.getMessage());
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                System.out.println("Failed to close stream: " + e.getMessage());
            }
        }
    }
}
