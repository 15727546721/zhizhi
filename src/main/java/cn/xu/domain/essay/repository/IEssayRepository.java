package cn.xu.domain.essay.repository;

import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.domain.essay.model.valobj.EssayType;
import cn.xu.domain.essay.model.vo.EssayVO;

import java.util.List;

/**
 * 话题仓储接口
 */
public interface IEssayRepository {

    /**
     * 保存随笔
     *
     * @param essayEntity 随笔实体
     * @return 话题ID
     */
    Long save(EssayEntity essayEntity);

    /**
     * 更新随笔
     *
     * @param essayEntity
     */
    void update(EssayEntity essayEntity);

    /**
     * 根据ID删除话题
     *
     * @param id 话题ID
     */
    void deleteById(Long id);

    /**
     * 批量删除话题
     *
     * @param ids 话题ID列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据ID查询话题
     *
     * @param id 话题ID
     * @return 话题实体
     */
    EssayEntity findById(Long id);

    /**
     * 获取话题总数
     *
     * @return 话题总数
     */
    Long count();

    /**
     * 根据条件查询随笔列表
     *
     * @param page
     * @param size
     * @param topic
     * @param essayType
     * @return
     */
    List<EssayWithUserAggregation> findEssayList(int page, int size, String topic, String essayType);
}