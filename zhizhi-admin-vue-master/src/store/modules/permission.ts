import { RouteRecordRaw } from "vue-router";
import { constantRoutes } from "@/router";
import { store } from "@/store";
import { listRoutes } from "@/api/menu";

const modules = import.meta.glob("../../views/**/**.vue");
const Layout = () => import("@/layout/index.vue");

/**
 * Use meta.role to determine if the current user has permission
 *
 * @param roles 用户角色集合
 * @param route 路由
 * @returns
 */
const hasPermission = (roles: string[], route: RouteRecordRaw) => {
  if (route.meta && route.meta.roles) {
    // 角色【超级管理员】拥有所有权限，忽略校验
    if (roles.includes("ROOT") || roles.includes("super_admin")) {
      return true;
    }
    return roles.some((role) => {
      if (route.meta?.roles) {
        return route.meta.roles.includes(role);
      }
    });
  }
  return false;
};

/**
 * 递归过滤有权限的异步(动态)路由
 *
 * @param routes 接口返回的异步(动态)路由
 * @param roles 用户角色集合
 * @returns 返回用户有权限的异步(动态)路由
 */
const filterAsyncRoutes = (routes: RouteRecordRaw[], roles: string[]) => {
  const asyncRoutes: any[] = [];
  
  // ROOT 角色直接返回所有路由，不做过滤
  const isRoot = roles.includes("ROOT") || roles.includes("super_admin");

  routes.forEach((route) => {
    const tmpRoute = { ...route }; // ES6扩展运算符复制新对象
    if (!route.name) {
      tmpRoute.name = route.name;
    }
    
    // ROOT 角色或有权限的路由才添加
    if (isRoot || hasPermission(roles, route) || !route.meta?.roles) {
      // 跳过BUTTON类型（没有component和path的菜单项）
      if (!tmpRoute.component && !tmpRoute.children) {
        return;
      }
      
      if (tmpRoute.component?.toString() == "Layout") {
        tmpRoute.component = Layout;
      } else if (tmpRoute.component) {
        // 尝试匹配组件路径
        const componentPath = tmpRoute.component.toString();
        const component = modules[`../../views${componentPath}.vue`];
        if (component) {
          tmpRoute.component = component;
        } else {
          // 组件不存在，跳过该路由
          console.warn(`[Router] Component not found: ${componentPath}`);
          return;
        }
      }

      if (tmpRoute.children) {
        tmpRoute.children = filterAsyncRoutes(tmpRoute.children, roles);
      }
      asyncRoutes.push(tmpRoute);
    }
  });
  return asyncRoutes;
};

// setup
export const usePermissionStore = defineStore("permission", () => {
  // state
  const routes = ref<RouteRecordRaw[]>([]);

  // actions
  function setRoutes(newRoutes: RouteRecordRaw[]) {
    routes.value = constantRoutes.concat(newRoutes);
  }
  /**
   * 生成动态路由
   *
   * @param roles 用户角色集合
   * @returns
   */
  function generateRoutes(roles: string[]) {
    return new Promise<RouteRecordRaw[]>((resolve, reject) => {
      // 接口获取所有路由
      listRoutes()
        .then(({ data: asyncRoutes }) => {
          // 根据角色获取有访问权限的路由
          const accessedRoutes = filterAsyncRoutes(asyncRoutes, roles);
          setRoutes(accessedRoutes);
          resolve(accessedRoutes);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }
  /**
   * 获取与激活的顶部菜单项相关的混合模式左侧菜单集合
   */
  const mixLeftMenus = ref<RouteRecordRaw[]>([]);
  function setMixLeftMenus(topMenuPath: string) {
    const matchedItem = routes.value.find((item) => item.path === topMenuPath);
    if (matchedItem && matchedItem.children) {
      mixLeftMenus.value = matchedItem.children;
    }
  }
  return {
    routes,
    setRoutes,
    generateRoutes,
    mixLeftMenus,
    setMixLeftMenus,
  };
});

// 非setup
export function usePermissionStoreHook() {
  return usePermissionStore(store);
}
