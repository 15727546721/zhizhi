package cn.xu.domain.post.service;

import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.web.model.dto.post.PostPageQueryRequest;
import cn.xu.api.web.model.vo.post.PostDetailResponse;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.api.web.model.vo.post.PostPageListResponse;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.common.response.PageResponse;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 帖子领域服务接口
 * 定义帖子相关的业务操作
 */
public interface IPostService {

    /**
     * 创建帖子
     *
     * @param postEntity 帖子实体
     * @return 帖子ID
     */
    Long createPost(PostEntity postEntity);
    
    /**
     * 创建或更新帖子草稿
     *
     * @param postEntity 帖子实体
     * @return 帖子ID
     */
    Long createOrUpdatePostDraft(PostEntity postEntity);
    
    /**
     * 上传封面图片
     *
     * @param imageFile 图片文件
     * @return 图片URL
     */
    String uploadCover(MultipartFile imageFile);
    
    /**
     * 分页查询帖子列表
     *
     * @param postRequest 查询参数
     * @return 分页结果
     */
    PageResponse<List<PostPageResponse>> listPost(SysPostQueryRequest postRequest);
    
    /**
     * 批量删除帖子
     *
     * @param postIds 帖子ID列表
     */
    void deletePosts(List<Long> postIds);
    
    /**
     * 更新帖子
     *
     * @param postEntity 帖子实体
     */
    void updatePost(PostEntity postEntity);
    
    /**
     * 获取所有已发布的帖子
     *
     * @return 帖子列表
     */
    List<PostEntity> getAllPublishedPosts();
    
    /**
     * 获取所有帖子
     *
     * @return 帖子列表
     */
    List<PostEntity> getAllPosts();
    
    /**
     * 根据ID查找帖子聚合根
     *
     * @param postId 帖子ID
     * @return 帖子聚合根
     */
    Optional<PostAggregate> findById(Long postId);
    
    /**
     * 根据用户ID获取帖子列表
     *
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<PostListResponse> getPostsByUserId(Long userId);
    
    /**
     * 根据ID查找帖子实体
     *
     * @param postId 帖子ID
     * @return 帖子实体
     */
    Optional<PostEntity> findPostEntityById(Long postId);
    
    /**
     * 获取用户帖子列表
     *
     * @param userId     用户ID
     * @param postStatus 帖子状态
     * @param pageNo     页码
     * @param pageSize   页面大小
     * @return 帖子列表
     */
    List<PostEntity> getUserPosts(Long userId, String postStatus, int pageNo, int pageSize);
    
    /**
     * 发布帖子
     *
     * @param postEntity 帖子实体
     * @param userId     用户ID
     */
    void publishPost(PostEntity postEntity, Long userId);
    
    /**
     * 获取草稿列表
     *
     * @param userId 用户ID
     * @return 草稿列表
     */
    List<PostListResponse> getDraftPostList(Long userId);
    
    /**
     * 删除帖子
     *
     * @param id     帖子ID
     * @param userId 用户ID
     */
    void deletePost(Long id, Long userId);
    
    /**
     * 增加帖子浏览量（带防刷机制）
     *
     * @param postId   帖子ID
     * @param clientIp 客户端IP
     * @param userId   用户ID
     */
    void viewPost(Long postId, String clientIp, Long userId);
    
    /**
     * 更新帖子热度分数
     *
     * @param postId 帖子ID
     */
    void updatePostHotScore(Long postId);
    
    /**
     * 根据分类ID分页获取帖子列表
     *
     * @param categoryId 分类ID
     * @param pageNo     页码
     * @param pageSize   页面大小
     * @return 帖子列表
     */
    List<PostEntity> getPostPageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize);
    
    /**
     * 分页获取帖子列表
     *
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    List<PostEntity> getPostPageList(Integer pageNo, Integer pageSize);
    
    /**
     * 分页获取帖子列表（支持排序）
     *
     * @param request 查询参数
     * @return 帖子列表
     */
    List<PostEntity> getPostPageList(PostPageQueryRequest request);
    
    /**
     * 获取帖子详情
     *
     * @param id            帖子ID
     * @param currentUserId 当前用户ID
     * @return 帖子详情
     */
    PostDetailResponse getPostDetail(Long id, Long currentUserId);
    
    /**
     * 分页获取帖子列表（支持排序）
     */
    List<PostPageListResponse> getPostPageListWithSort(PostPageQueryRequest request);

    
    /**
     * 采纳回答
     *
     * @param postId   帖子ID
     * @param answerId 回答ID
     * @param userId   用户ID
     */
    void acceptAnswer(Long postId, Long answerId, Long userId);
    
    /**
     * 根据类型查找帖子
     *
     * @param type     帖子类型
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    List<PostEntity> findPostsByType(PostType type, Integer pageNo, Integer pageSize);
    
    /**
     * 统计指定类型的帖子数量
     *
     * @param type 帖子类型
     * @return 帖子数量
     */
    long countPostsByType(PostType type);
    
    /**
     * 查找热门帖子
     *
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    List<PostEntity> findHotPosts(Integer pageNo, Integer pageSize);
    
    /**
     * 统计热门帖子数量
     *
     * @return 帖子数量
     */
    long countHotPosts();
    
    /**
     * 查找推荐帖子
     *
     * @param userId   用户ID
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @param categoryId 分类ID，可选
     * @return 帖子列表
     */
    List<PostEntity> findRecommendedPosts(Long userId, Integer pageNo, Integer pageSize, Long categoryId);
    
    /**
     * 统计推荐帖子数量
     *
     * @param userId 用户ID
     * @param categoryId 分类ID，可选
     * @return 帖子数量
     */
    long countRecommendedPosts(Long userId, Long categoryId);
    
    /**
     * 根据标签ID查找帖子
     *
     * @param tagId    标签ID
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    List<PostEntity> findPostsByTagId(Long tagId, Integer pageNo, Integer pageSize);
    
    /**
     * 统计指定标签的帖子数量
     *
     * @param tagId 标签ID
     * @return 帖子数量
     */
    long countPostsByTagId(Long tagId);
    
    /**
     * 根据用户ID列表获取帖子
     *
     * @param userIds  用户ID列表
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    List<PostEntity> getPostsByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize);
    
    /**
     * 统计指定用户的帖子数量
     *
     * @param userIds 用户ID列表
     * @return 帖子数量
     */
    long countPostsByUserIds(List<Long> userIds);
    
    /**
     * 查找精选帖子
     *
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    List<PostEntity> findFeaturedPosts(Integer pageNo, Integer pageSize);
    
    /**
     * 统计精选帖子数量
     *
     * @return 帖子数量
     */
    long countFeaturedPosts();
    
    /**
     * 根据问题ID查找回答
     *
     * @param questionId 问题ID
     * @param pageNo     页码
     * @param pageSize   页面大小
     * @return 回答列表
     */
    List<PostEntity> findAnswersByQuestionId(Long questionId, Integer pageNo, Integer pageSize);
    
    /**
     * 获取帖子总数
     *
     * @return 帖子总数
     */
    long countAllPosts();

    /**
     * 增加帖子分享数
     * @param postId 帖子ID
     */
    void increasePostShareCount(Long postId);


    /**
     * 根据帖子类型查找相关帖子
     *
     * @param postType 帖子类型
     * @param excludePostId 要排除的帖子ID（当前帖子）
     * @param limit 返回的帖子数量限制
     * @return 相关帖子列表
     */
    List<PostEntity> findRelatedPostsByType(PostType postType, Long excludePostId, int limit);

}