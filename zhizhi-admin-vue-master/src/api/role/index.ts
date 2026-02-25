import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { RoleQuery, RolePageResult, RoleForm } from "./types";

/**
 * 获取角色分页数据
 *
 * @param queryParams
 */
export function getRolePage(
  queryParams?: RoleQuery
): AxiosPromise<RolePageResult> {
  return request({
    url: "/api/system/role/list",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取角色的菜单ID集合
 *
 * @param queryParams
 */
export function getRoleMenuIds(id: number): AxiosPromise<number[]> {
  return request({
    url: "/api/system/role/getRoleMenuIds",
    method: "get",
    params: {
      roleId: id,
    },
  });
}

/**
 * 分配菜单权限给角色
 *
 * @param queryParams
 */
export function updateRoleMenus(
  roleId: number,
  data: number[]
): AxiosPromise<any> {
  return request({
    url: "/api/system/role/updateRoleMenus",
    method: "post",
    data: {
      roleId: roleId,
      menuIds: data,
    },
  });
}

/**
 * 添加角色
 *
 * @param data
 */
export function addRole(data: RoleForm) {
  return request({
    url: "/api/system/role/add",
    method: "post",
    data: data,
  });
}

/**
 * 更新角色
 *
 * @param id
 * @param data
 */
export function updateRole(data: RoleForm) {
  return request({
    url: "/api/system/role/update",
    method: "post",
    data: data,
  });
}

/**
 * 批量删除角色，多个以英文逗号(,)分割
 *
 * @param ids
 */
export function deleteRoles(ids: any) {
  return request({
    url: "/api/system/role/delete",
    method: "post",
    data: ids,
  });
}
