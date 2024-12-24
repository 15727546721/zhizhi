package cn.xu.api.controller.web.article;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleListDTO {
    private int id;
    private String title;
    private String coverUrl;
    private String description;
    private String nickname;
    private Date createTime;
    private long viewCount;
    private long likeCount;
    private List<String> tagNameList;
}
