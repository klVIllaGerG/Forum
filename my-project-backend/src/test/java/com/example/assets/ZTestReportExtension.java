package com.example.assets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.*;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class ZTestReportExtension implements AfterEachCallback, BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    // 使用静态变量收集所有测试文件的结果
    private static List<ReportInfo> listInfo = new ArrayList<>();
    private static String beginTime;
    private static long totalTime;
    private static int testsPass = 0;
    private static int testsFail = 0;
    private static int testsSkip = 0;
    private static boolean initialized = false;
    private static Map<Method, Long> methodStartTime = new HashMap<>();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // 只在第一次初始化
        if (!initialized) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            beginTime = formatter.format(new Date());
            initialized = true;
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // 记录每个测试方法的开始时间
        Method method = context.getTestMethod().orElseThrow();
        methodStartTime.put(method, System.currentTimeMillis());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElseThrow();
        long startTime = methodStartTime.get(method);
        long endTime = System.currentTimeMillis();
        long spendTime = endTime - startTime;
        totalTime += spendTime;

        String testName = context.getDisplayName();
        boolean success = context.getExecutionException().isEmpty();
        String status = success ? "成功" : "失败";

        if (success) {
            testsPass++;
        } else {
            testsFail++;
        }

        String description = context.getElement()
                .map(el -> el.getAnnotation(DisplayName.class))
                .map(DisplayName::value)
                .orElse("");

        ReportInfo info = new ReportInfo();
        info.setName(testName);
        info.setSpendTime(spendTime + "ms");
        info.setStatus(status);
        info.setClassName(context.getTestClass().map(Class::getName).orElse(""));
        info.setMethodName(context.getTestMethod().map(Method::getName).orElse(""));
        info.setDescription(description);
        info.setLog(new ArrayList<>());

        context.getExecutionException().ifPresent(ex -> {
            List<String> log = new ArrayList<>();
            log.add(ex.toString());
            for (StackTraceElement element : ex.getStackTrace()) {
                log.add("    " + element);
            }
            info.setLog(log);
        });

        listInfo.add(info);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // 确保只在根上下文中生成报告

            Map<String, Object> result = new HashMap<>();
            result.put("testName", "集成测试报告");
            result.put("testPass", testsPass);
            result.put("testFail", testsFail);
            result.put("testSkip", testsSkip);
            result.put("testAll", testsPass + testsFail + testsSkip);
            result.put("beginTime", beginTime);
            result.put("totalTime", totalTime + "ms");
            result.put("testResult", listInfo);

            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            String templatePath = System.getProperty("user.dir") + File.separator + "src/test/java/com/example/Assets/template";
            String outputPath = System.getProperty("user.dir") + File.separator + "src/test/java/com/example/Assets/report_integration.html";
            String template = read(templatePath);

            try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath)), "UTF-8"))) {
                template = template.replaceFirst("\\$\\{resultData\\}", Matcher.quoteReplacement(gson.toJson(result)));
                output.write(template);
            }
        }

    private String read(String path) throws IOException {
        File file = new File(path);
        StringBuilder sb = new StringBuilder();

        try (InputStream is = new FileInputStream(file)) {
            int index;
            byte[] b = new byte[1024];
            while ((index = is.read(b)) != -1) {
                sb.append(new String(b, 0, index));
            }
        }

        return sb.toString();
    }

    @Data
    public static class ReportInfo {
        private String name;
        private String className;
        private String methodName;
        private String description;
        private String spendTime;
        private String status;
        private List<String> log;
    }
}
