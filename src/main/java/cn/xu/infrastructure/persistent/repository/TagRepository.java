package cn.xu.infrastructure.persistent.repository;

import cn.xu.common.Constants;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.persistent.dao.ITagDao;
import cn.xu.infrastructure.persistent.po.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TagRepository implements ITagRepository {

    @Resource
    private ITagDao tagDao;

    @Override
    public void save(TagEntity tag) {
        log.info("保存标签: {}", tag);
        Tag build = Tag.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .build();
        try {
            tagDao.insert(build);
        } catch (Exception e) {
            log.error("保存标签失败: {}", e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "保存标签失败");
        }
    }

    @Override
    public List<TagEntity> queryTagList(int page, int size) {
        // 查询标签列表，使用分页
        List<Tag> tagList = tagDao.selectListByPage((page - 1) * size, size);
        log.info("查询标签列表，返回结果：{}", tagList);

        // 将 Tag 对象转换为 TagEntity 对象
        List<TagEntity> tagEntityList = tagList.stream()
                .map(this::convertToTagEntity)
                .collect(Collectors.toList());

        return tagEntityList;
    }

    @Override
    public void update(TagEntity tagEntity) {
        log.info("更新标签: {}", tagEntity);
        try {
            Tag tag = Tag.builder()
                    .id(tagEntity.getId())
                    .name(tagEntity.getName())
                    .description(tagEntity.getDescription())
                    .build();
            tagDao.update(tag);
        } catch (Exception e) {
            log.error("更新标签失败: {}", e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "更新标签失败");
        }
    }

    @Override
    public void delete(List<Long> idList) {
        log.info("删除标签ID列表: {}", idList);
        try {
            tagDao.delete(idList);
        } catch (Exception e) {
            log.error("删除标签失败: {}", e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "删除标签失败");
        }
    }

    @Override
    public Tag findById(Long tagId) {
        if (tagId == null) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "标签ID不能为空");
        }
        log.info("查询标签ID: {}", tagId);
        try {
            Tag tag = tagDao.selectById(tagId);
            log.info("查询标签结果: {}", tag);
            return tag;
        } catch (Exception e) {
            log.error("查询标签失败: {}", e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "查询标签失败");
        }
    }

    @Override
    public List<TagEntity> getTagSelectList() {
        // 查询标签列表，使用分页
        List<Tag> tagList = tagDao.selectList();
        log.info("查询标签列表，返回结果：{}", tagList);

        // 将 Tag 对象转换为 TagEntity 对象
        List<TagEntity> tagEntityList = tagList.stream()
                .map(this::convertToTagEntity)
                .collect(Collectors.toList());

        return tagEntityList;
    }

    private TagEntity convertToTagEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagEntity tagEntity = new TagEntity();
        tagEntity.setId(tag.getId());
        tagEntity.setName(tag.getName());
        tagEntity.setDescription(tag.getDescription());
        tagEntity.setCreateTime(tag.getCreateTime());
        tagEntity.setUpdateTime(tag.getUpdateTime());
        return tagEntity;
    }
}
