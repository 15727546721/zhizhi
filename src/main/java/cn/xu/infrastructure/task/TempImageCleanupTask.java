package cn.xu.infrastructure.task;

import cn.xu.domain.file.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class TempImageCleanupTask {

    @Resource
    private MinioService minioService;

    /**
     * 每小时执行一次，清理超过2小时的临时图片
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupTempImages() {
        try {
            log.info("开始清理临时图片");
            List<String> allFiles = minioService.listFiles();
            LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
            
            // 筛选出需要清理的临时图片
            List<String> tempImages = allFiles.stream()
                .filter(file -> {
                    if (!file.startsWith("temp/")) {
                        return false;
                    }
                    // 从文件路径中提取时间戳 (temp/yyyyMMdd/HHmmss_filename)
                    try {
                        String[] parts = file.split("/");
                        if (parts.length >= 3) {
                            String dateStr = parts[1];
                            String timeStr = parts[2].split("_")[0];
                            LocalDateTime fileTime = LocalDateTime.parse(
                                dateStr + timeStr, 
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                            );
                            return fileTime.isBefore(twoHoursAgo);
                        }
                    } catch (Exception e) {
                        log.warn("解析文件时间戳失败: {}", file);
                    }
                    return false;
                })
                .collect(Collectors.toList());
                
            if (!tempImages.isEmpty()) {
                minioService.deleteTopicImages(tempImages);
                log.info("清理临时图片完成，共清理{}个文件", tempImages.size());
            }
        } catch (Exception e) {
            log.error("清理临时图片失败", e);
        }
    }
} 