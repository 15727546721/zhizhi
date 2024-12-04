package cn.xu.api.dto.permission;


import lombok.Data;

@Data
public class RoleRequest {
    private Long id;
    private String name;
    private String code;
    private String desc;
}
