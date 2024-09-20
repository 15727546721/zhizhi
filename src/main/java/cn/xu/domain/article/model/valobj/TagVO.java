package cn.xu.domain.article.model.valobj;

import lombok.Data;

@Data
public class TagVO {
    private String name;

    private String description;

    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
}
