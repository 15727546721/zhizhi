package cn.xu.domain.permission.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuComponentVO {
    Layout("Layout", "后台首页，全局路由的第一个节点页面"),
    ;

    private final String name;
    private final String desc;
}
