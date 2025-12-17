package cn.xu.repository.read.elastic.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(indexName = "comments")
//@Setting(settingPath = "/es-config/comments-settings.json")
public class CommentIndex {
    @Id
    private Long id;

    @Field(type = FieldType.Integer)
    private Integer targetType;

    @Field(type = FieldType.Long)
    private Long targetId;

    @Field(type = FieldType.Long)
    private Long parentId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long replyUserId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Long)
    private Long replyCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    @Field(type = FieldType.Double)
    private Double hotScore;

    // 嵌套字段用于存储用户信息
    @Field(type = FieldType.Nested)
    private UserInfo user;

    @Field(type = FieldType.Nested)
    private UserInfo replyUser;

    // 嵌套字段用于存储子评论
    @Field(type = FieldType.Nested)
    private List<SubComment> topReplies;

    @Data
    public static class UserInfo {
        @Field(type = FieldType.Long)
        private Long id;
        @Field(type = FieldType.Keyword)
        private String nickname;
        @Field(type = FieldType.Keyword)
        private String avatar;
    }

    @Data
    public static class SubComment {
        @Field(type = FieldType.Long)
        private Long id;
        @Field(type = FieldType.Text)
        private String content;
        @Field(type = FieldType.Long)
        private Long likeCount;
        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
        private LocalDateTime createTime;
        @Field(type = FieldType.Nested)
        private UserInfo user;
        @Field(type = FieldType.Nested)
        private UserInfo replyUser;
    }
}