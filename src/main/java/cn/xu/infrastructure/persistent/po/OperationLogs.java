package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于存储系统管理操作的日志信息
 * @TableName operation_logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogs implements Serializable {
    /**
     * 日志记录的唯一标识
     */
    private Long id;

    /**
     * 操作类型，例如“创建”、“更新”、“删除”等
     */
    private String operationType;

    /**
     * 操作的详细描述
     */
    private String operationDescription;

    /**
     * 执行操作的用户ID
     */
    private Long userId;

    /**
     * 执行操作的用户名
     */
    private String username;

    /**
     * 操作发生的时间，默认当前时间
     */
    private Date createTime;

    /**
     * 执行操作的IP地址
     */
    private String ipAddress;

    /**
     * 操作的结果状态，1表示成功，0表示失败
     */
    private Integer status;

    /**
     * 额外的操作信息，存储为JSON格式的字符串
     */
    private String additionalInfo;

    private static final long serialVersionUID = 1L;

}