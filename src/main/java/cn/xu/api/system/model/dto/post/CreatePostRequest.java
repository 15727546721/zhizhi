package cn.xu.api.system.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private Long categoryId;
    private List<Long> tagIds; // 标签ID列表
    private List<Long> topicIds; // 话题ID列表
    private String type; // 帖子类型
    private String status; // 帖子状态，DRAFT表示草稿，PUBLISHED表示发布
}