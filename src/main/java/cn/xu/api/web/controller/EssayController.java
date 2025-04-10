package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.essay.EssayQueryRequest;
import cn.xu.api.web.model.dto.essay.EssaySaveRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.essay.command.CreateEssayCommand;
import cn.xu.domain.essay.model.vo.EssayVO;
import cn.xu.domain.essay.service.impl.EssayService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.infrastructure.common.annotation.ApiOperationLog;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/essay")
@Tag(name = "话题接口")
public class EssayController {

    @Resource
    private EssayService essayService;
    @Resource
    private ILikeService likeService;
    @Resource
    private MinioService minioService;

    @Operation(summary = "创建随笔")
    @SaCheckLogin
    @PostMapping("/create")
    public ResponseEntity<Void> createTopic(@RequestBody EssaySaveRequest request) {
        log.info("开始创建随笔，请求参数: {}", request);
        CreateEssayCommand command = new CreateEssayCommand();
        command.setContent(request.getContent());
        command.setTopics(request.getTopics());
        command.setUserId(StpUtil.getLoginIdAsLong());
        command.setImages(request.getImages());
        essayService.createTopic(command);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "获取随笔列表")
    @ApiOperationLog(description = "获取随笔列表")
    @PostMapping("/list/page")
    public ResponseEntity<List<EssayVO>> queryEssayList(@RequestBody EssayQueryRequest request) {
        log.info("开始获取随笔列表，请求参数: {}", request.getPageNo());
        if (request.getType() == null) {
            throw new BusinessException("查询类型不能为空!");
        }
        List<EssayVO> essayEntities = essayService.getEssayList(request);

        return ResponseEntity.<List<EssayVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(essayEntities)
                .build();
    }

    @Operation(summary = "删除随笔")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        essayService.deleteTopic(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "上传随笔图片")
    @PostMapping("/upload/images")
    public ResponseEntity<List<String>> uploadTopicImages(@RequestParam("files") MultipartFile[] files) {
        log.info("开始上传随笔图片，文件数量: {}", files.length);
        List<String> imageUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                // 检查文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能上传图片文件");
                }

                // 生成临时文件存储路径 (temp/年月日/时间戳_文件名)
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String filePath = String.format("temp/%s/%s_%s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                        timestamp,
                        UUID.randomUUID());

                // 上传文件并获取URL
                String imageUrl = minioService.uploadTopicImage(file, filePath);
                imageUrls.add(imageUrl);
            }

            return ResponseEntity.<List<String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(imageUrls)
                    .build();
        } catch (Exception e) {
            // 上传失败时，删除已上传的图片
            if (!imageUrls.isEmpty()) {
                minioService.deleteTopicImages(imageUrls);
            }
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传图片失败：" + e.getMessage());
        }
    }

    @GetMapping("/like/{id}")
    @Operation(summary = "随笔点赞")
    @SaCheckLogin
    public ResponseEntity<Void> likeEssay(@PathVariable Long id) {
        log.info("开始点赞随笔，id: {}", id);
        long userId = StpUtil.getLoginIdAsLong();
        likeService.like(userId, LikeType.ESSAY.getValue(), id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @GetMapping("/unlike/{id}")
    @Operation(summary = "取消随笔点赞")
    @SaCheckLogin
    public ResponseEntity<?> unlikeEssay(@PathVariable("id") Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        likeService.unlike(userId, LikeType.ESSAY.getValue(), id);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章取消点赞成功")
                .build();
    }
} 