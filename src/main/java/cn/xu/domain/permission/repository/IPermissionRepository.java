package cn.xu.domain.permission.repository;

import java.util.List;

public interface IPermissionRepository {

    List<String> findRolesByUserid(Long userid);
}
