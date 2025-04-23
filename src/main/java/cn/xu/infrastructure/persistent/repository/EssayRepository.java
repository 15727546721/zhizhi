package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.domain.essay.repository.IEssayRepository;
import cn.xu.infrastructure.persistent.dao.IEssayDao;
import cn.xu.infrastructure.persistent.po.Essay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class EssayRepository implements IEssayRepository {

    @Resource
    private IEssayDao essayDao;

    @Override
    public Long save(EssayEntity essayEntity) {
        Long id = essayDao.insert(essayEntity);
        if (id == null) {
            log.info("保存随笔失败");
        }
        return id;
    }

    @Override
    public void update(EssayEntity essayEntity) {
        essayDao.update(essayEntity);
    }

    @Override
    public void deleteById(Long id) {
        essayDao.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        essayDao.deleteByIds(ids);
    }

    @Override
    public EssayEntity findById(Long id) {
        Essay essay = essayDao.findById(id);
        return essay != null ? convertToTopicEntity(essay) : null;
    }

    @Override
    public Long count() {
        try {
            return essayDao.count();
        } catch (Exception e) {
            log.error("查询话题总数失败", e);
            return 0L;
        }
    }

    @Override
    public List<EssayWithUserAggregation> findEssayList(int page, int size, String topic, String essayType) {
        int offset = (page - 1) * size;
        return essayDao.findEssayList(offset, size, topic, essayType);
    }

    /**
     * 将PO对象转换为领域实体
     */
    private EssayEntity convertToTopicEntity(Essay essay) {
        if (essay == null) {
            return null;
        }

        return EssayEntity.builder()
                .id(essay.getId())
                .userId(essay.getUserId())
                .content(essay.getContent())
                .images(essay.getImages())
                .createTime(essay.getCreateTime())
                .updateTime(essay.getUpdateTime())
                .build();
    }
} 