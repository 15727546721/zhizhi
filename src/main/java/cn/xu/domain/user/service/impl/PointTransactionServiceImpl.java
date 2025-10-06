package cn.xu.domain.user.service.impl;

import cn.xu.domain.user.model.entity.MemberEntity;
import cn.xu.domain.user.model.entity.PointChangeTypeConfigEntity;
import cn.xu.domain.user.model.entity.PointTransactionEntity;
import cn.xu.domain.user.repository.IPointTransactionRepository;
import cn.xu.domain.user.service.IPointTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 积分流水领域服务实现
 * 处理积分流水相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointTransactionServiceImpl implements IPointTransactionService {
    
    @Resource
    private IPointTransactionRepository pointTransactionRepository;
    
    @Override
    public void addUserPoints(Long userId, Long points, String changeType, String description, Long relatedId) {
        log.info("增加用户积分 - userId: {}, points: {}, changeType: {}, description: {}", userId, points, changeType, description);
        
        try {
            // 检查积分变动类型配置
            PointChangeTypeConfigEntity config = pointTransactionRepository.findPointChangeTypeConfigByType(changeType);
            if (config == null || !config.isActive()) {
                throw new IllegalStateException("积分变动类型配置不存在或未启用");
            }
            
            // 检查每日限制
            if (!config.isNoLimit()) {
                int todayCount = pointTransactionRepository.countTodayEarnTransactions(userId, changeType);
                if (todayCount >= config.getDailyLimit()) {
                    throw new IllegalStateException("今日该类型积分获取已达到上限");
                }
            }
            
            // 获取或创建会员信息
            MemberEntity memberEntity = pointTransactionRepository.findMemberByUserId(userId);
            if (memberEntity == null) {
                memberEntity = MemberEntity.createNewMember(userId);
            }
            
            // 增加积分
            memberEntity.addPoints(points);
            
            // 创建积分流水记录
            PointTransactionEntity transactionEntity = PointTransactionEntity.createAddTransaction(
                    userId, points, changeType, description, memberEntity.getCurrentPoints());
            
            // 设置关联ID
            if (relatedId != null) {
                transactionEntity.setRelatedId(relatedId);
            }
            
            // 保存数据
            pointTransactionRepository.savePointTransaction(transactionEntity);
            pointTransactionRepository.saveMember(memberEntity);
            
            // 记录日志
            log.info("用户积分增加成功 - userId: {}, points: {}, changeType: {}, description: {}", userId, points, changeType, description);
        } catch (Exception e) {
            log.error("增加用户积分失败 - userId: {}, points: {}, changeType: {}, description: {}", userId, points, changeType, description, e);
            throw new RuntimeException("增加用户积分失败", e);
        }
    }
    
    @Override
    public void consumeUserPoints(Long userId, Long points, String changeType, String description, Long relatedId) {
        log.info("消费用户积分 - userId: {}, points: {}, changeType: {}, description: {}", userId, points, changeType, description);
        
        try {
            // 获取会员信息
            MemberEntity memberEntity = pointTransactionRepository.findMemberByUserId(userId);
            if (memberEntity == null) {
                throw new IllegalStateException("用户会员信息不存在");
            }
            
            // 消费积分
            memberEntity.consumePoints(points);
            
            // 创建积分流水记录
            PointTransactionEntity transactionEntity = PointTransactionEntity.createConsumeTransaction(
                    userId, points, changeType, description, memberEntity.getCurrentPoints());
            
            // 设置关联ID
            if (relatedId != null) {
                transactionEntity.setRelatedId(relatedId);
            }
            
            // 保存数据
            pointTransactionRepository.savePointTransaction(transactionEntity);
            pointTransactionRepository.saveMember(memberEntity);
            
            // 记录日志
            log.info("用户积分消费成功 - userId: {}, points: {}, changeType: {}, description: {}", userId, points, changeType, description);
        } catch (Exception e) {
            log.error("消费用户积分失败 - userId: {}, points: {}, changeType: {}, description: {}", userId, points, changeType, description, e);
            throw new RuntimeException("消费用户积分失败", e);
        }
    }
    
    @Override
    public List<PointTransactionEntity> getUserPointTransactions(Long userId, int page, int size) {
        try {
            return pointTransactionRepository.findPointTransactionsByUserId(userId, page, size);
        } catch (Exception e) {
            log.error("获取用户积分流水记录失败 - userId: {}, page: {}, size: {}", userId, page, size, e);
            throw new RuntimeException("获取用户积分流水记录失败", e);
        }
    }
    
    @Override
    public List<PointTransactionEntity> getUserPointTransactionsByType(Long userId, String changeType, int page, int size) {
        try {
            return pointTransactionRepository.findPointTransactionsByUserIdAndType(userId, changeType, page, size);
        } catch (Exception e) {
            log.error("获取用户指定类型的积分流水记录失败 - userId: {}, changeType: {}, page: {}, size: {}", userId, changeType, page, size, e);
            throw new RuntimeException("获取用户指定类型的积分流水记录失败", e);
        }
    }
    
    @Override
    public MemberEntity getMemberInfo(Long userId) {
        try {
            MemberEntity memberEntity = pointTransactionRepository.findMemberByUserId(userId);
            if (memberEntity == null) {
                // 如果用户还没有会员记录，创建一个新的
                memberEntity = MemberEntity.createNewMember(userId);
                pointTransactionRepository.saveMember(memberEntity);
            }
            return memberEntity;
        } catch (Exception e) {
            log.error("获取用户会员信息失败 - userId: {}", userId, e);
            throw new RuntimeException("获取用户会员信息失败", e);
        }
    }
    
    @Override
    public void addUserExp(Long userId, Long exp, String reason) {
        log.info("增加用户经验值 - userId: {}, exp: {}, reason: {}", userId, exp, reason);
        
        try {
            // 获取或创建会员信息
            MemberEntity memberEntity = pointTransactionRepository.findMemberByUserId(userId);
            if (memberEntity == null) {
                memberEntity = MemberEntity.createNewMember(userId);
            }
            
            // 增加经验值并检查升级
            memberEntity.addExpAndCheckLevelUp(exp);
            
            // 保存会员信息
            pointTransactionRepository.saveMember(memberEntity);
            
            // 记录日志
            log.info("用户经验值增加成功 - userId: {}, exp: {}, reason: {}", userId, exp, reason);
        } catch (Exception e) {
            log.error("增加用户经验值失败 - userId: {}, exp: {}, reason: {}", userId, exp, reason, e);
            throw new RuntimeException("增加用户经验值失败", e);
        }
    }
    
    @Override
    public List<MemberEntity> getPointsRanking(int limit) {
        try {
            return pointTransactionRepository.findPointsRanking(limit);
        } catch (Exception e) {
            log.error("获取积分排行榜失败 - limit: {}", limit, e);
            throw new RuntimeException("获取积分排行榜失败", e);
        }
    }
    
    @Override
    public List<MemberEntity> getLevelRanking(int limit) {
        try {
            return pointTransactionRepository.findLevelRanking(limit);
        } catch (Exception e) {
            log.error("获取等级排行榜失败 - limit: {}", limit, e);
            throw new RuntimeException("获取等级排行榜失败", e);
        }
    }
    
    @Override
    public void freezeMemberAccount(Long userId) {
        log.info("冻结用户账户 - userId: {}", userId);
        
        try {
            MemberEntity memberEntity = pointTransactionRepository.findMemberByUserId(userId);
            if (memberEntity == null) {
                throw new IllegalStateException("用户会员信息不存在");
            }
            
            memberEntity.freeze();
            pointTransactionRepository.saveMember(memberEntity);
            
            log.info("用户账户冻结成功 - userId: {}", userId);
        } catch (Exception e) {
            log.error("冻结用户账户失败 - userId: {}", userId, e);
            throw new RuntimeException("冻结用户账户失败", e);
        }
    }
    
    @Override
    public void unfreezeMemberAccount(Long userId) {
        log.info("解冻用户账户 - userId: {}", userId);
        
        try {
            MemberEntity memberEntity = pointTransactionRepository.findMemberByUserId(userId);
            if (memberEntity == null) {
                throw new IllegalStateException("用户会员信息不存在");
            }
            
            memberEntity.unfreeze();
            pointTransactionRepository.saveMember(memberEntity);
            
            log.info("用户账户解冻成功 - userId: {}", userId);
        } catch (Exception e) {
            log.error("解冻用户账户失败 - userId: {}", userId, e);
            throw new RuntimeException("解冻用户账户失败", e);
        }
    }
}