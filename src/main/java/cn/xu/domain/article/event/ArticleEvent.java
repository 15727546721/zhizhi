package cn.xu.domain.article.event;

import lombok.Data;
import lombok.Getter;

@Data
public class ArticleEvent {
    private Long articleId;
    private Long userId;
    private EventType type;
    /**
     * 点赞、收藏、评论等事件是否是新增的还是取消的
     */
    private boolean isAdd;

    @Getter
    public enum EventType {

        LIKE("like", "点赞"),
        COLLECT("collect", "收藏"),
        COMMENT("comment", "评论"),
        ;
        private String type;
        private String info;

        EventType(String type, String info) {
            this.type = type;
            this.info = info;
        }
    }
}

