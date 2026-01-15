package cn.xu.elasticsearch.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "posts")
public class PostIndex {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String coverUrl;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long viewCount;
    
    @Field(type = FieldType.Long)
    private Long shareCount;
    
    @Field(type = FieldType.Boolean)
    private Boolean isFeatured;

    @Field(type = FieldType.Long)
    private Long favoriteCount;

    @Field(type = FieldType.Long)
    private Long commentCount;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime publishTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;

    @Field(type = FieldType.Double)
    private Double hotScore;
}
