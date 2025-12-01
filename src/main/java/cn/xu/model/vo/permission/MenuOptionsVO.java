package cn.xu.model.vo.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单选项VO
 * 用于前端菜单选择树的数据结构
 *
 * @author xu
 * @since 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOptionsVO {
    
    private Long id;
    private String label;
    private List<MenuOptionsVO> children;
}
