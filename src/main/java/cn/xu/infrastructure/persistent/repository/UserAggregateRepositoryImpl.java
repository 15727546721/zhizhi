package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.user.model.aggregate.UserAggregate;
import cn.xu.domain.user.model.entity.RoleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.repository.IUserAggregateRepository;
import cn.xu.infrastructure.persistent.converter.UserConverter;
import cn.xu.infrastructure.persistent.converter.UserRoleConverter;
import cn.xu.infrastructure.persistent.dao.RoleMapper;
import cn.xu.infrastructure.persistent.dao.UserMapper;
import cn.xu.infrastructure.persistent.dao.UserRoleMapper;
import cn.xu.infrastructure.persistent.po.Role;
import cn.xu.infrastructure.persistent.po.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户聚合根仓储实现类
 * 通过Converter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAggregateRepositoryImpl implements IUserAggregateRepository {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserConverter userConverter;
    private final UserRoleConverter userRoleConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(UserAggregate aggregate) {
        try {
            log.info("[用户聚合根] 开始保存用户聚合根");
            
            // 保存用户基本信息
            UserEntity userEntity = aggregate.getUser();
            User userPO = userConverter.toDataObject(userEntity);
            
            if (userPO.getId() == null) {
                userMapper.insert(userPO);
                userEntity.setId(userPO.getId());
                log.info("[用户聚合根] 新增用户成功，用户ID: {}", userPO.getId());
            } else {
                userMapper.update(userPO);
                log.info("[用户聚合根] 更新用户成功，用户ID: {}", userPO.getId());
            }
            
            // 保存用户角色关联关系
            List<RoleEntity> roles = aggregate.getRoles();
            if (roles != null && !roles.isEmpty()) {
                // 先删除原有角色关联
                userRoleMapper.deleteByUserId(userPO.getId());
                
                // 插入新的角色关联
                List<Long> roleIds = roles.stream()
                        .map(RoleEntity::getId)
                        .collect(Collectors.toList());
                userRoleMapper.saveUserRoles(userPO.getId(), roleIds);
                
                log.info("[用户聚合根] 保存用户角色关联成功，角色数量: {}", roles.size());
            }
            
            return userPO.getId();
        } catch (Exception e) {
            log.error("[用户聚合根] 保存用户聚合根失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserAggregate aggregate) {
        try {
            log.info("[用户聚合根] 开始更新用户聚合根，用户ID: {}", aggregate.getId());
            
            // 更新用户基本信息
            UserEntity userEntity = aggregate.getUser();
            User userPO = userConverter.toDataObject(userEntity);
            userMapper.update(userPO);
            
            // 更新用户角色关联关系
            List<RoleEntity> roles = aggregate.getRoles();
            if (roles != null && !roles.isEmpty()) {
                // 先删除原有角色关联
                userRoleMapper.deleteByUserId(userPO.getId());
                
                // 插入新的角色关联
                List<Long> roleIds = roles.stream()
                        .map(RoleEntity::getId)
                        .collect(Collectors.toList());
                userRoleMapper.saveUserRoles(userPO.getId(), roleIds);
            }
            
            log.info("[用户聚合根] 更新用户聚合根成功");
        } catch (Exception e) {
            log.error("[用户聚合根] 更新用户聚合根失败，用户ID: {}", aggregate.getId(), e);
            throw e;
        }
    }

    @Override
    public Optional<UserAggregate> findById(Long id) {
        try {
            log.info("[用户聚合根] 开始查询用户聚合根，用户ID: {}", id);
            
            // 查询用户基本信息
            User user = userMapper.selectById(id);
            if (user == null) {
                log.info("[用户聚合根] 用户不存在，用户ID: {}", id);
                return Optional.empty();
            }
            
            UserEntity userEntity = userConverter.toDomainEntity(user);
            
            // 查询用户角色信息
            List<Role> roles = roleMapper.findRolesByUserId(id);
            List<RoleEntity> roleEntities = userRoleConverter.toDomainEntities(roles);
            
            UserAggregate aggregate = UserAggregate.builder()
                    .id(id)
                    .user(userEntity)
                    .roles(roleEntities)
                    .build();
            
            log.info("[用户聚合根] 查询用户聚合根成功，角色数量: {}", roleEntities.size());
            return Optional.of(aggregate);
        } catch (Exception e) {
            log.error("[用户聚合根] 查询用户聚合根失败，用户ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserAggregate> findByUsername(String username) {
        try {
            log.info("[用户聚合根] 开始根据用户名查询用户聚合根，用户名: {}", username);
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                log.info("[用户聚合根] 用户不存在，用户名: {}", username);
                return Optional.empty();
            }
            
            return findById(user.getId());
        } catch (Exception e) {
            log.error("[用户聚合根] 根据用户名查询用户聚合根失败，用户名: {}", username, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserAggregate> findByEmail(Email email) {
        try {
            log.info("[用户聚合根] 开始根据邮箱查询用户聚合根，邮箱: {}", email.getValue());
            
            User user = userMapper.selectByEmail(email.getValue());
            if (user == null) {
                log.info("[用户聚合根] 用户不存在，邮箱: {}", email.getValue());
                return Optional.empty();
            }
            
            return findById(user.getId());
        } catch (Exception e) {
            log.error("[用户聚合根] 根据邮箱查询用户聚合根失败，邮箱: {}", email.getValue(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<UserAggregate> findByPage(Integer pageNo, Integer pageSize) {
        try {
            log.info("[用户聚合根] 开始分页查询用户聚合根，页码: {}, 页面大小: {}", pageNo, pageSize);
            
            int offset = (pageNo - 1) * pageSize;
            List<User> users = userMapper.selectByPage(offset, pageSize);
            
            if (users.isEmpty()) {
                log.info("[用户聚合根] 分页查询结果为空");
                return Collections.emptyList();
            }
            
            List<UserAggregate> aggregates = users.stream()
                    .map(user -> {
                        UserEntity userEntity = userConverter.toDomainEntity(user);
                        List<Role> roles = roleMapper.findRolesByUserId(user.getId());
                        List<RoleEntity> roleEntities = userRoleConverter.toDomainEntities(roles);
                        
                        return UserAggregate.builder()
                                .id(user.getId())
                                .user(userEntity)
                                .roles(roleEntities)
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("[用户聚合根] 分页查询用户聚合根成功，返回数量: {}", aggregates.size());
            return aggregates;
        } catch (Exception e) {
            log.error("[用户聚合根] 分页查询用户聚合根失败，页码: {}, 页面大小: {}", pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<UserAggregate> findByIds(List<Long> userIds) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            log.info("[用户聚合根] 开始批量查询用户聚合根，用户数量: {}", userIds.size());
            
            List<User> users = userMapper.findByIds(userIds);
            if (users.isEmpty()) {
                log.info("[用户聚合根] 批量查询结果为空");
                return Collections.emptyList();
            }
            
            List<UserAggregate> aggregates = users.stream()
                    .map(user -> {
                        UserEntity userEntity = userConverter.toDomainEntity(user);
                        List<Role> roles = roleMapper.findRolesByUserId(user.getId());
                        List<RoleEntity> roleEntities = userRoleConverter.toDomainEntities(roles);
                        
                        return UserAggregate.builder()
                                .id(user.getId())
                                .user(userEntity)
                                .roles(roleEntities)
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("[用户聚合根] 批量查询用户聚合根成功，返回数量: {}", aggregates.size());
            return aggregates;
        } catch (Exception e) {
            log.error("[用户聚合根] 批量查询用户聚合根失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        try {
            log.info("[用户聚合根] 开始删除用户聚合根，用户ID: {}", id);
            
            // 先删除用户角色关联
            userRoleMapper.deleteByUserId(id);
            
            // 再删除用户基本信息
            userMapper.deleteById(id);
            
            log.info("[用户聚合根] 删除用户聚合根成功");
        } catch (Exception e) {
            log.error("[用户聚合根] 删除用户聚合根失败，用户ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            int count = userMapper.countByUsername(username);
            boolean exists = count > 0;
            log.info("[用户聚合根] 检查用户名是否存在，用户名: {}, 结果: {}", username, exists);
            return exists;
        } catch (Exception e) {
            log.error("[用户聚合根] 检查用户名是否存在失败，用户名: {}", username, e);
            return false;
        }
    }

    @Override
    public boolean existsByEmail(Email email) {
        try {
            int count = userMapper.countByEmail(email.getValue());
            boolean exists = count > 0;
            log.info("[用户聚合根] 检查邮箱是否存在，邮箱: {}, 结果: {}", email.getValue(), exists);
            return exists;
        } catch (Exception e) {
            log.error("[用户聚合根] 检查邮箱是否存在失败，邮箱: {}", email.getValue(), e);
            return false;
        }
    }
}