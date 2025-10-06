package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.user.model.entity.MemberEntity;
import cn.xu.domain.user.model.entity.PointChangeTypeConfigEntity;
import cn.xu.domain.user.model.entity.PointTransactionEntity;
import cn.xu.domain.user.repository.IPointTransactionRepository;
import cn.xu.infrastructure.persistent.converter.UserIncentiveConverter;
import cn.xu.infrastructure.persistent.dao.MemberMapper;
import cn.xu.infrastructure.persistent.dao.PointChangeTypeConfigMapper;
import cn.xu.infrastructure.persistent.dao.PointTransactionMapper;
import cn.xu.infrastructure.persistent.po.Member;
import cn.xu.infrastructure.persistent.po.PointChangeTypeConfig;
import cn.xu.infrastructure.persistent.po.PointTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分流水仓储实现
 * 遵循DDD原则，通过转换器处理领域实体与持久化对象的转换
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PointTransactionRepository implements IPointTransactionRepository {
    
    @Resource
    private PointTransactionMapper pointTransactionMapper;
    
    @Resource
    private MemberMapper memberMapper;
    
    @Resource
    private PointChangeTypeConfigMapper pointChangeTypeConfigMapper;
    
    @Resource
    private UserIncentiveConverter userIncentiveConverter;
    
    @Override
    public void savePointTransaction(PointTransactionEntity pointTransactionEntity) {
        PointTransaction pointTransaction = userIncentiveConverter.toPointTransactionPO(pointTransactionEntity);
        pointTransactionMapper.insert(pointTransaction);
        pointTransactionEntity.setId(pointTransaction.getId());
    }
    
    @Override
    public List<PointTransactionEntity> findPointTransactionsByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<PointTransaction> pointTransactions = pointTransactionMapper.findByUserId(userId, offset, size);
        return pointTransactions.stream()
                .map(userIncentiveConverter::toPointTransactionEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PointTransactionEntity> findPointTransactionsByUserIdAndType(Long userId, String changeType, int page, int size) {
        int offset = (page - 1) * size;
        List<PointTransaction> pointTransactions = pointTransactionMapper.findByUserIdAndType(userId, changeType, offset, size);
        return pointTransactions.stream()
                .map(userIncentiveConverter::toPointTransactionEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public int countTodayEarnTransactions(Long userId, String changeType) {
        return pointTransactionMapper.countTodayEarnTransactions(userId, changeType);
    }
    
    @Override
    public MemberEntity findMemberByUserId(Long userId) {
        Member member = memberMapper.findByUserId(userId);
        return userIncentiveConverter.toMemberEntity(member);
    }
    
    @Override
    public void saveMember(MemberEntity memberEntity) {
        Member member = userIncentiveConverter.toMemberPO(memberEntity);
        if (member.getId() == null) {
            memberMapper.insert(member);
            memberEntity.setId(member.getId());
        } else {
            memberMapper.update(member);
        }
    }
    
    @Override
    public PointChangeTypeConfigEntity findPointChangeTypeConfigByType(String changeType) {
        PointChangeTypeConfig config = pointChangeTypeConfigMapper.findByChangeType(changeType);
        return userIncentiveConverter.toPointChangeTypeConfigEntity(config);
    }
    
    @Override
    public List<PointChangeTypeConfigEntity> findAllActivePointChangeTypeConfigs() {
        List<PointChangeTypeConfig> configs = pointChangeTypeConfigMapper.findAllActive();
        return configs.stream()
                .map(userIncentiveConverter::toPointChangeTypeConfigEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MemberEntity> findPointsRanking(int limit) {
        List<Member> members = memberMapper.findPointsRanking(limit);
        return members.stream()
                .map(userIncentiveConverter::toMemberEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MemberEntity> findLevelRanking(int limit) {
        List<Member> members = memberMapper.findLevelRanking(limit);
        return members.stream()
                .map(userIncentiveConverter::toMemberEntity)
                .collect(Collectors.toList());
    }
}