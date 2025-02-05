package cn.xu.infrastructure.persistent.handler;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(BusinessType.class)
public class BusinessTypeHandler extends BaseTypeHandler<BusinessType> {

    /**
     * 将BusinessType枚举转换为数据库中的字符串值
     *
     * @param ps PreparedStatement对象
     * @param i 参数索引
     * @param parameter BusinessType枚举参数
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BusinessType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    /**
     * 从数据库中获取字符串值并转换为BusinessType枚举
     *
     * @param rs ResultSet对象
     * @param columnName 列名
     * @return BusinessType枚举
     * @throws SQLException SQL异常
     */
    @Override
    public BusinessType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : BusinessType.valueOf(value);
    }

    /**
     * 从数据库中获取字符串值并转换为BusinessType枚举
     *
     * @param rs ResultSet对象
     * @param columnIndex 列索引
     * @return BusinessType枚举
     * @throws SQLException SQL异常
     */
    @Override
    public BusinessType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : BusinessType.valueOf(value);
    }

    /**
     * 从数据库中获取字符串值并转换为BusinessType枚举
     *
     * @param cs CallableStatement对象
     * @param columnIndex 列索引
     * @return BusinessType枚举
     * @throws SQLException SQL异常
     */
    @Override
    public BusinessType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : BusinessType.valueOf(value);
    }
} 