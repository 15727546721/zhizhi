package cn.xu.domain.post.model.aggregate;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子和作者聚合根
 * DDD设计中的聚合根，包含帖子实体和作者信息
 * 负责维护帖子与作者之间的一致性边界
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAndAuthorAggregate {

    /**
     * 帖子实体
     */
    private PostEntity post;

    /**
     * 作者实体
     */
    private UserEntity author;

    /**
     * 获取帖子ID
     */
    public Long getPostId() {
        return post != null ? post.getId() : null;
    }

    /**
     * 获取作者ID
     */
    public Long getAuthorId() {
        return author != null ? author.getId() : null;
    }

    /**
     * 获取帖子标题
     */
    public String getPostTitle() {
        return post != null ? post.getTitleValue() : null;
    }

    /**
     * 获取作者昵称
     */
    public String getAuthorNickname() {
        return author != null ? author.getNickname() : null;
    }

    /**
     * 验证聚合根的完整性
     */
    public void validate() {
        if (post == null) {
            throw new IllegalStateException("帖子实体不能为空");
        }
        if (author == null) {
            throw new IllegalStateException("作者实体不能为空");
        }
    }
}