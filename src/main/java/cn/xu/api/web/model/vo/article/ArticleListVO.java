package cn.xu.api.web.model.vo.article;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleListVO {
    ArticleEntity article;
    UserEntity user;
    private List<String> tagNameList;
}
