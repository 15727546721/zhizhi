package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.CommentImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface CommentImageMapper {

    /**
     * 根据评论id，获取评论图片
     * @param commentId
     * @return
     */
    List<CommentImage> selectImagesByCommentId(@Param("commentId") Long commentId);
}




