package cn.xu.domain.permission.model.vo;

public enum MenuTypeVO {
    CATALOG("CATALOG", "目录"),
    MENU("MENU", "菜单"),
    BUTTON("BUTTON", "按钮"),
    EXTLINK("EXTLINK", "外链");

    private String code;
    private String name;

    MenuTypeVO(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
