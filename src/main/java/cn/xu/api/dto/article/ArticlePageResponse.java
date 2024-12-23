package cn.xu.api.dto.article;

import cn.xu.common.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticlePageResponse {
    private Long id;
    /**
     * 作者昵称
     */
    private String nickname;
    private String title;
    private String description;
    private String coverUrl;
    private Long viewCount;
    @JsonFormat(pattern = DateUtil.FORMAT_STRING, timezone="GMT+8")
    private Date createTime;
    private String categoryName;
    private String tagNames;
}
