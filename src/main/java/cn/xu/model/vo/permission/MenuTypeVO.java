package cn.xu.model.vo.permission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuTypeVO {
    CATALOG("CATALOG", "目录"),
    MENU("MENU", "菜单"),
    BUTTON("BUTTON", "按钮"),
    EXTLINK("EXTLINK", "外链");

    private final String code;
    private final String name;
}