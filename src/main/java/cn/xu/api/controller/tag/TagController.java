package cn.xu.api.controller.tag;

import cn.xu.api.dto.request.article.TagDTO;
import cn.xu.api.dto.request.common.PageDTO;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.repository.ITagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/tag")
@RestController
public class TagController {

    @Resource
    private ITagRepository tagRepository;

    @PostMapping("/add")
    public ResponseEntity addTag(@RequestBody TagDTO tagDTO) {
        if (ObjectUtils.isEmpty(tagDTO)) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.NULL_PARAMETER.getCode())
                    .info(Constants.ResponseCode.NULL_PARAMETER.getInfo())
                    .build();
        }
        TagEntity tagEntity = TagEntity.builder()
                .name(tagDTO.getName())
                .description(tagDTO.getDescription())
                .build();
        tagRepository.save(tagEntity);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("保存标签成功")
                .build();
    }

    @GetMapping("/list")
    public ResponseEntity getTagList(@ModelAttribute PageDTO pageDTO) {
        int page = pageDTO.getPage();
        int size = pageDTO.getSize();
        log.info("查询标签列表: page={}, size={}", page, size);
        List<TagEntity> tagEntityList = tagRepository.queryTagList(page, size);
        return ResponseEntity.<List<TagEntity>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagEntityList)
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity updateTag(@RequestBody TagDTO tagDTO) {
        if (ObjectUtils.isEmpty(tagDTO)) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.NULL_PARAMETER.getCode())
                    .info(Constants.ResponseCode.NULL_PARAMETER.getInfo())
                    .build();
        }
        TagEntity tagEntity = TagEntity.builder()
                .id(tagDTO.getId())
                .name(tagDTO.getName())
                .description(tagDTO.getDescription())
                .build();
        tagRepository.update(tagEntity);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("更新标签成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteTag(@RequestBody List<Long> idList) {
        if (idList.isEmpty()) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.NULL_PARAMETER.getCode())
                    .info(Constants.ResponseCode.NULL_PARAMETER.getInfo())
                    .build();
        }
        tagRepository.delete(idList);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("删除标签成功")
                .build();
    }
}

