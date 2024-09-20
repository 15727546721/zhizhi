package cn.xu.domain.article.model.aggregate;

import cn.xu.common.Constants;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.TagVO;
import cn.xu.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleAggregate {

    private ArticleEntity articleEntity;
    private List<TagVO> tags = new ArrayList<>(); // 初始化为一个空列表

    // 添加标签的业务逻辑
    public void addTag(TagVO tag) {
        if (tag.isValid()) {
            this.tags.add(tag);
        } else {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "无效标签");
        }
    }
}
