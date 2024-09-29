package cn.xu.domain.article.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {
    private Long id;
    private String name;

    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
}
