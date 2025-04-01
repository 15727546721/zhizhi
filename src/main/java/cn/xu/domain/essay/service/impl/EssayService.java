package cn.xu.domain.essay.service.impl;

import cn.xu.api.web.model.dto.essay.EssayQueryRequest;
import cn.xu.api.web.model.dto.essay.TopicResponse;
import cn.xu.domain.essay.command.CreateEssayCommand;
import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.domain.essay.model.valobj.EssayType;
import cn.xu.domain.essay.model.vo.EssayVO;
import cn.xu.domain.essay.repository.IEssayRepository;
import cn.xu.domain.essay.service.IEssayService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EssayService implements IEssayService {

    @Resource
    private IEssayRepository essayRepository;

    @Resource
    private MinioService minioService;

    @Resource
    private CommentRepository commentRepository;

    /**
     * 创建话题
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTopic(CreateEssayCommand command) {
        List<String> formalImages = new ArrayList<>();
        try {

            // 1. 如果有图片，将临时图片移动到永久目录
            if (command.getImages() != null && !command.getImages().isEmpty()) {
                formalImages = minioService.moveToFormal(command.getImages());
            }

            // 2. 转换为领域实体
            String topics = command.getTopics().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            String images = formalImages.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            EssayEntity essay = EssayEntity.builder()
                    .userId(command.getUserId())
                    .content(command.getContent())
                    .images(images)
                    .topics(topics)
                    .build();

            // 3. 验证
            essay.validate();

            // 4. 保存
            Long topicId = essayRepository.save(essay);

        } catch (Exception e) {
            // 发生异常时，删除已移动的永久图片
            if (!formalImages.isEmpty()) {
                minioService.deleteTopicImages(formalImages);
            }
            log.error("发布话题失败！", e);
            throw new BusinessException("发布话题失败！");
        }
    }

    @Override
    public void deleteTopic(Long id) {

    }

    /**
     * 批量删除话题
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopics(List<Long> ids) {
        essayRepository.deleteByIds(ids);
    }

    @Override
    public List<EssayVO> getEssayList(EssayQueryRequest request) {
        int page = request.getPageNo();
        int size = request.getPageSize();
        String topic = request.getTopic();
        EssayType essayType = EssayType.valueOf(request.getType().toUpperCase());
        List<EssayWithUserAggregation> essayList
                = essayRepository.findEssayList(page, size, topic, essayType.name());
        List<EssayVO> voList = new LinkedList<>();
        for (EssayWithUserAggregation essay : essayList) {
            String[] topics = null;
            String[] images = null;
            if (essay.getTopics() != null &&!essay.getTopics().isEmpty()) {
                topics = essay.getTopics().split(",");
            }
            if (essay.getImages() != null && !essay.getImages().isEmpty()) {
                images = essay.getImages().split(",");
            }
            EssayVO vo = EssayVO.builder()
                    .id(essay.getId())
                    .user(essay.getUser())
                    .content(essay.getContent())
                    .images(images)
                    .topics(topics)
                    .createTime(essay.getCreateTime())
                    .updateTime(essay.getUpdateTime())
                    .build();
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public EssayEntity getEssayById(Long essayId) {
        return essayRepository.findById(essayId);
    }

    @Override
    public TopicResponse getTopicDetail(Long id) {
        return null;
    }

}