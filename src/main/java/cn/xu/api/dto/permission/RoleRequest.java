package cn.xu.api.dto.permission;


import lombok.Data;

@Data
public class RoleRequest {
    private String name;
    private String code;
    private String desc;
}
