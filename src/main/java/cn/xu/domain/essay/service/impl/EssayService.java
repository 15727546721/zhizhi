package cn.xu.domain.essay.service.impl;

import cn.xu.api.web.model.dto.essay.EssayQueryRequest;
import cn.xu.api.web.model.dto.essay.TopicResponse;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.essay.command.CreateEssayCommand;
import cn.xu.domain.essay.event.EssayDeletedEvent;
import cn.xu.domain.essay.event.EssayEventPublisher;
import cn.xu.domain.essay.model.aggregate.EssayAggregate;
import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.domain.essay.model.valobj.EssayType;
import cn.xu.domain.essay.model.vo.EssayResponse;
import cn.xu.domain.essay.repository.IEssayRepository;
import cn.xu.domain.essay.service.IEssayService;
import cn.xu.domain.file.service.IFileStorageService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.infrastructure.persistent.repository.CommentRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 随笔领域服务实现
 * 负责随笔相关的核心业务逻辑处理
 */
@Slf4j
@Service
public class EssayService implements IEssayService {

    @Resource
    private IEssayRepository essayRepository;

    @Resource
    private IFileStorageService fileStorageService;

    @Resource
    private EssayEventPublisher essayEventPublisher;

    @Resource
    private MinioService minioService;

    @Resource
    private CommentRepositoryImpl commentRepositoryImpl;

    /**
     * 创建随笔
     * 使用聚合根管理随笔的完整生命周期
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTopic(CreateEssayCommand command) {
        if (command == null) {
            throw new BusinessException("创建随笔命令不能为空");
        }
        
        // 验证命令参数
        validateCreateCommand(command);
        
        List<String> formalImages = new ArrayList<>();
        try {
            // 1. 如果有图片，将临时图片移动到永久目录
            if (command.getImages() != null && !command.getImages().isEmpty()) {
                formalImages = minioService.moveToFormal(command.getImages());
                log.info("图片移动成功，临时图片数量: {}, 正式图片数量: {}", 
                        command.getImages().size(), formalImages.size());
            }

            // 2. 创建随笔聚合根
            EssayAggregate essayAggregate = EssayAggregate.create(
                    command.getUserId(),
                    command.getContent(),
                    formalImages,
                    command.getTopics()
            );

            // 3. 验证聚合完整性
            essayAggregate.validate();

            // 4. 保存聚合根
            Long essayId = essayRepository.save(essayAggregate.getEssayEntity());

            log.info("随笔创建成功，随笔ID: {}, 用户ID: {}, 内容长度: {}, 图片数量: {}, 话题数量: {}",
                    essayId, command.getUserId(), 
                    command.getContent() != null ? command.getContent().length() : 0,
                    formalImages.size(),
                    command.getTopics() != null ? command.getTopics().size() : 0);

        } catch (Exception e) {
            // 发生异常时，删除已移动的永久图片
            if (!formalImages.isEmpty()) {
                try {
                    minioService.deleteTopicImages(formalImages);
                    log.info("异常回滚，删除图片成功: {}", formalImages);
                } catch (Exception deleteException) {
                    log.error("异常回滚时删除图片失败: {}", formalImages, deleteException);
                }
            }
            
            log.error("创建随笔失败，用户ID: {}", command.getUserId(), e);
            
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("创建随笔失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 验证创建随笔命令
     */
    private void validateCreateCommand(CreateEssayCommand command) {
        if (command.getUserId() == null || command.getUserId() <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (command.getContent() == null || command.getContent().trim().isEmpty()) {
            throw new BusinessException("随笔内容不能为空");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("随笔ID不能为空");
        }
        
        try {
            // 查询随笔是否存在
            EssayEntity essayEntity = essayRepository.findById(id);
            if (essayEntity == null) {
                throw new BusinessException("随笔不存在");
            }
            
            // 删除随笔相关的图片
            if (essayEntity.getImages() != null && !essayEntity.getImages().isEmpty()) {
                List<String> imageUrls = essayEntity.getImages().getImageUrls();
                try {
                    fileStorageService.deleteFiles(imageUrls);
                    log.info("删除随笔相关图片成功，随笔ID: {}, 图片数量: {}", id, imageUrls.size());
                } catch (Exception e) {
                    log.warn("删除随笔相关图片失败，随笔ID: {}", id, e);
                }
            }
            
            // 删除随笔
            essayRepository.deleteById(id);
            
            // 发布随笔删除事件
            try {
                EssayDeletedEvent event = EssayDeletedEvent.builder()
                        .essayId(id)
                        .userId(essayEntity.getUserId())
                        .deletedTime(LocalDateTime.now())
                        .build();
                essayEventPublisher.publishEssayDeletedEvent(event);
                log.info("发布随笔删除事件成功，随笔ID: {}", id);
            } catch (Exception e) {
                log.warn("发布随笔删除事件失败，随笔ID: {}", id, e);
            }
            
            log.info("随笔删除成功，随笔ID: {}", id);
            
        } catch (Exception e) {
            log.error("删除随笔失败，随笔ID: {}", id, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("删除随笔失败: " + e.getMessage());
            }
        }
    }

    /**
     * 批量删除随笔
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopics(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("随笔ID列表不能为空");
        }
        
        try {
            essayRepository.deleteByIds(ids);
            log.info("批量删除随笔成功，删除数量: {}", ids.size());
            
        } catch (Exception e) {
            log.error("批量删除随笔失败，ID列表: {}", ids, e);
            throw new BusinessException("批量删除随笔失败: " + e.getMessage());
        }
    }

    @Override
    public List<EssayResponse> getEssayList(EssayQueryRequest request) {
        if (request == null) {
            throw new BusinessException("查询请求不能为空");
        }
        
        // 验证分页参数
        int page = request.getPageNo() > 0 ? request.getPageNo() : 1;
        int size = request.getPageSize() > 0 ? request.getPageSize() : 10;
        String topic = request.getTopic();
        
        // 验证和转换随笔类型
        EssayType essayType;
        try {
            essayType = EssayType.valueOf(request.getType().toUpperCase());
        } catch (Exception e) {
            log.warn("无效的随笔类型: {}, 使用默认类型 NEW", request.getType());
            essayType = EssayType.NEW;
        }
        
        try {
            List<EssayWithUserAggregation> essayList = essayRepository.findEssayList(
                    page, size, topic, essayType.name());
            
            List<EssayResponse> voList = convertToVOList(essayList);
            
            log.debug("查询随笔列表成功，页码: {}, 大小: {}, 话题: {}, 类型: {}, 结果数量: {}", 
                    page, size, topic, essayType.name(), voList.size());
            
            return voList;
            
        } catch (Exception e) {
            log.error("查询随笔列表失败，请求: {}", request, e);
            throw new BusinessException("查询随笔列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 将聚合对象转换为VO对象列表
     */
    private List<EssayResponse> convertToVOList(List<EssayWithUserAggregation> essayList) {
        if (essayList == null || essayList.isEmpty()) {
            return new LinkedList<>();
        }
        
        List<EssayResponse> voList = new LinkedList<>();
        for (EssayWithUserAggregation essay : essayList) {
            try {
                EssayResponse vo = convertToVO(essay);
                if (vo != null) {
                    voList.add(vo);
                }
            } catch (Exception e) {
                log.warn("转换随笔VO失败，随笔ID: {}", essay.getId(), e);
            }
        }
        
        return voList;
    }
    
    /**
     * 将聚合对象转换为VO对象
     */
    private EssayResponse convertToVO(EssayWithUserAggregation essay) {
        if (essay == null) {
            return null;
        }
        
        String[] topics = null;
        String[] images = null;
        
        // 安全地处理话题字符串
        if (essay.getTopics() != null && !essay.getTopics().trim().isEmpty()) {
            topics = essay.getTopics().split(",");
        }
        
        // 安全地处理图片字符串
        if (essay.getImages() != null && !essay.getImages().trim().isEmpty()) {
            images = essay.getImages().split(",");
        }
        
        return EssayResponse.builder()
                .id(essay.getId())
                .user(essay.getUser())
                .content(essay.getContent())
                .images(images)
                .topics(topics)
                .likeCount(essay.getLikeCount() != null ? essay.getLikeCount() : 0L)
                .commentCount(essay.getCommentCount() != null ? essay.getCommentCount() : 0L)
                .createTime(essay.getCreateTime())
                .updateTime(essay.getUpdateTime())
                .build();
    }

    @Override
    public EssayEntity getEssayById(Long essayId) {
        if (essayId == null || essayId <= 0) {
            throw new BusinessException("随笔ID不能为空");
        }
        
        try {
            EssayEntity entity = essayRepository.findById(essayId);
            if (entity == null) {
                log.warn("随笔不存在，随笔ID: {}", essayId);
            }
            return entity;
            
        } catch (Exception e) {
            log.error("查询随笔详情失败，随笔ID: {}", essayId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("查询随笔详情失败: " + e.getMessage());
            }
        }
    }

    @Override
    public TopicResponse getTopicDetail(Long id) {
        // 实现话题详情查询逻辑
        if (id == null || id <= 0) {
            throw new BusinessException("话题ID不能为空");
        }
        
        try {
            // 查询话题详情
            EssayEntity essayEntity = essayRepository.findById(id);
            if (essayEntity == null) {
                log.warn("话题不存在，话题ID: {}", id);
                return null;
            }
            
            // 构建话题响应对象
            TopicResponse response = TopicResponse.builder()
                    .id(essayEntity.getId())
                    .userId(essayEntity.getUserId())
                    .content(essayEntity.getContent().toString()) // 使用toString()方法替代getValue()
                    .images(Arrays.asList(essayEntity.getImages().getImageUrls().toArray(new String[0])))
                    .createTime(essayEntity.getCreateTime())
                    .updateTime(essayEntity.getUpdateTime())
                    .build();
            
            log.info("查询话题详情成功，话题ID: {}", id);
            return response;
            
        } catch (Exception e) {
            log.error("查询话题详情失败，话题ID: {}", id, e);
            throw new BusinessException("查询话题详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 增加随笔点赞数
     * 
     * @param essayId 随笔ID
     * @param userId 点赞用户ID
     */
    public void likeEssay(Long essayId, Long userId) {
        if (essayId == null || essayId <= 0) {
            throw new BusinessException("随笔ID不能为空");
        }
        
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        
        try {
            // 查询随笔
            EssayEntity essayEntity = essayRepository.findById(essayId);
            if (essayEntity == null) {
                throw new BusinessException("随笔不存在");
            }
            
            // 创建聚合根并执行点赞操作
            EssayAggregate aggregate = EssayAggregate.restore(
                    essayEntity.getId(),
                    essayEntity.getUserId(),
                    essayEntity.getContentString(),
                    essayEntity.getImagesString(),
                    essayEntity.getTopicsString(),
                    essayEntity.getLikeCount(),
                    essayEntity.getCommentCount(),
                    essayEntity.getCreateTime(),
                    essayEntity.getUpdateTime()
            );
            
            aggregate.like(userId);
            
            // 更新到数据库
            essayRepository.update(aggregate.getEssayEntity());
            
            log.info("随笔点赞成功，随笔ID: {}, 用户ID: {}", essayId, userId);
            
        } catch (Exception e) {
            log.error("随笔点赞失败，随笔ID: {}, 用户ID: {}", essayId, userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("随笔点赞失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 取消随笔点赞
     * 
     * @param essayId 随笔ID
     * @param userId 用户ID
     */
    public void unlikeEssay(Long essayId, Long userId) {
        if (essayId == null || essayId <= 0) {
            throw new BusinessException("随笔ID不能为空");
        }
        
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        
        try {
            // 查询随笔
            EssayEntity essayEntity = essayRepository.findById(essayId);
            if (essayEntity == null) {
                throw new BusinessException("随笔不存在");
            }
            
            // 创建聚合根并执行取消点赞操作
            EssayAggregate aggregate = EssayAggregate.restore(
                    essayEntity.getId(),
                    essayEntity.getUserId(),
                    essayEntity.getContentString(),
                    essayEntity.getImagesString(),
                    essayEntity.getTopicsString(),
                    essayEntity.getLikeCount(),
                    essayEntity.getCommentCount(),
                    essayEntity.getCreateTime(),
                    essayEntity.getUpdateTime()
            );
            
            aggregate.unlike(userId);
            
            // 更新到数据库
            essayRepository.update(aggregate.getEssayEntity());
            
            log.info("取消随笔点赞成功，随笔ID: {}, 用户ID: {}", essayId, userId);
            
        } catch (Exception e) {
            log.error("取消随笔点赞失败，随笔ID: {}, 用户ID: {}", essayId, userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("取消随笔点赞失败: " + e.getMessage());
            }
        }
    }
}