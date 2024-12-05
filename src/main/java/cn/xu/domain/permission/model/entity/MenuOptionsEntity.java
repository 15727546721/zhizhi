package cn.xu.domain.permission.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class MenuOptionsEntity {
    private Long id;

    private String label;

    private List<MenuOptionsEntity> children;
}
