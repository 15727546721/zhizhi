package cn.xu.domain.permission.model.vo;

public enum MenuComponentVO {
    Layout("主页", "后台首页，全局路由的第一个节点页面"),
    ;

    private String name;

    private String desc;

    MenuComponentVO(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
