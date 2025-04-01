package cn.xu.domain.essay.model.valobj;

import lombok.Getter;

@Getter
public enum EssayType {

    NEW("NEW", "最新"),
    HOT("HOT", "热门"),
    BEST("BEST", "精华"),
    ;

    private String value;
    private String desc;

    EssayType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
