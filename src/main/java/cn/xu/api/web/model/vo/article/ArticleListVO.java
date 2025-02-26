package cn.xu.api.web.model.vo.article;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleListVO {
    private int id;
    private String title;
    private String coverUrl;
    private String description;
    private String nickname;
    private LocalDateTime createTime;
    private LocalDateTime publishTime;
    private long viewCount;
    private long likeCount;
    private List<String> tagNameList;
}
