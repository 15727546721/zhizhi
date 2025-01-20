package cn.xu.domain.topic.service.impl;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.topic.command.CreateTopicCategoryCommand;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.repository.ITopicCategoryRepository;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class TopicCategoryServiceImpl implements ITopicCategoryService {

    @Resource
    private ITopicCategoryRepository topicCategoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicCategoryEntity createCategory(CreateTopicCategoryCommand command) {
        // 1. 检查名称是否已存在
        TopicCategoryEntity existingCategory = topicCategoryRepository.findByName(command.getName());
        if (existingCategory != null) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "分类名称已存在");
        }

        // 2. 创建分类实体
        TopicCategoryEntity categoryEntity = TopicCategoryEntity.builder()
                .name(command.getName())
                .description(command.getDescription())
                .sort(command.getSort())
                .build();

        // 3. 验证
        categoryEntity.validate();

        // 4. 保存
        Long categoryId = topicCategoryRepository.save(categoryEntity);

        // 5. 返回完整的分类信息
        return topicCategoryRepository.findById(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, CreateTopicCategoryCommand command) {
        // 1. 检查分类是否存在
        TopicCategoryEntity existingCategory = topicCategoryRepository.findById(id);
        if (existingCategory == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "分类不存在");
        }

        // 2. 检查名称是否已被其他分类使用
        TopicCategoryEntity categoryWithSameName = topicCategoryRepository.findByName(command.getName());
        if (categoryWithSameName != null && !categoryWithSameName.getId().equals(id)) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "分类名称已存在");
        }

        // 3. 创建新的分类实体
        TopicCategoryEntity newCategory = TopicCategoryEntity.builder()
                .id(id)
                .name(command.getName())
                .description(command.getDescription())
                .sort(command.getSort())
                .build();

        // 4. 验证
        newCategory.validate();

        // 5. 更新
        existingCategory.update(newCategory);
        topicCategoryRepository.update(existingCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 1. 检查分类是否存在
        TopicCategoryEntity existingCategory = topicCategoryRepository.findById(id);
        if (existingCategory == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "分类不存在");
        }

        // 2. 删除分类
        topicCategoryRepository.deleteById(id);
    }

    @Override
    public TopicCategoryEntity getCategory(Long id) {
        if (id == null) {
            return null; // 如果ID为空，直接返回null
        }
        TopicCategoryEntity category = topicCategoryRepository.findById(id);
        if (category == null) {
            log.warn("分类ID为 " + id + " 的分类不存在");
            return null; // 返回null而不是抛出异常
        }
        return category;
    }

    @Override
    public List<TopicCategoryEntity> getAllCategories() {
        return topicCategoryRepository.findAll();
    }
} 