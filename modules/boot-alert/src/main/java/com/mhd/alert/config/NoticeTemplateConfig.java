package com.mhd.alert.config;

import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.enums.NoticeTemplatePresetEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class NoticeTemplateConfig implements CommandLineRunner {
    private static final Long DEFAULT_USER_ID = -1L;
    @Getter
    private static final Map<Integer, NoticeTemplate> PRESET_TEMPLATE = new HashMap<>(16);

    @Override
    public void run(String... args) throws Exception {
        try {
            int total = 0;
            int successCount = 0;
            int failCount = 0;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:templates/*.*");
            for (Resource resource : resources) {
                if (resource.getFilename() == null || (!resource.getFilename().endsWith("txt") && !resource.getFilename().endsWith("html"))) {
                    log.warn("[Alert] Ignore the template file {}.", resource.getFilename());
                    continue;
                }
                total++;
                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    String content = new String(bytes, StandardCharsets.UTF_8);
                    NoticeTemplate template = new NoticeTemplate();
                    String name = resource.getFilename().replace(".txt", "").replace(".html", "");
                    String[] names = name.split("-");
                    if (names.length != 2) {
                        log.warn("[Alert] Ignore the template file {}.", resource.getFilename());
                        continue;
                    }
                    int type = Integer.parseInt(names[0]);
                    name = names[1];
                    template.setName(name);
                    template.setType(type);
                    template.setPreset(NoticeTemplatePresetEnum.PRESET.getCode());
                    template.setContent(content);
                    template.setCreateBy(DEFAULT_USER_ID);
                    template.setCreateTime(LocalDateTime.now());
                    template.setUpdateBy(DEFAULT_USER_ID);
                    template.setUpdateTime(LocalDateTime.now());
                    PRESET_TEMPLATE.put(template.getType(), template);
                    successCount++;
                } catch (IOException e) {
                    log.error("[Alert] Load default notice template error {}", e.getMessage(), e);
                    log.error("[Alert] Ignore this template file: {}.", resource.getFilename());
                    failCount++;
                }
            }
            log.info("[Alert] Load default notice template. total: {}, success: {}, fail: {}", total, successCount, failCount);
        } catch (Exception e) {
            log.error("[Alert] Load default notice template error", e);
            throw new RuntimeException(e);
        }
    }
}
