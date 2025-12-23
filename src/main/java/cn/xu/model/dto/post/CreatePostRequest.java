package cn.xu.model.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePostRequest {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private List<Long> tagIds; // 标签ID列表
    private String type; // 帖子类型
    private String status; // 帖子状态，DRAFT表示草稿，PUBLISHED表示发布
}
