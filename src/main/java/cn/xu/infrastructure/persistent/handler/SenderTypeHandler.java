package cn.xu.infrastructure.persistent.handler;

import cn.xu.domain.notification.model.valueobject.SenderType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(SenderType.class)
public class SenderTypeHandler extends BaseTypeHandler<SenderType> {

    /**
     * 将SenderType枚举转换为数据库中的字符串值
     *
     * @param ps PreparedStatement对象
     * @param i 参数索引
     * @param parameter SenderType枚举参数
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SenderType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    /**
     * 从数据库中获取字符串值并转换为SenderType枚举
     *
     * @param rs ResultSet对象
     * @param columnName 列名
     * @return SenderType枚举
     * @throws SQLException SQL异常
     */
    @Override
    public SenderType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : SenderType.valueOf(value);
    }

    /**
     * 从数据库中获取字符串值并转换为SenderType枚举
     *
     * @param rs ResultSet对象
     * @param columnIndex 列索引
     * @return SenderType枚举
     * @throws SQLException SQL异常
     */
    @Override
    public SenderType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : SenderType.valueOf(value);
    }

    /**
     * 从数据库中获取字符串值并转换为SenderType枚举
     *
     * @param cs CallableStatement对象
     * @param columnIndex 列索引
     * @return SenderType枚举
     * @throws SQLException SQL异常
     */
    @Override
    public SenderType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : SenderType.valueOf(value);
    }
} 