package cn.xu.api.web.model.dto.essay;

import lombok.Data;

import java.util.List;

@Data
public class EssaySaveRequest {
    private String content;
    private List<String> topics;
    private List<String> images;
}
