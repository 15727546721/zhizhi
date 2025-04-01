package cn.xu.domain.essay.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicPageQuery {
    private Integer pageNum;
    private Integer pageSize;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }
} 