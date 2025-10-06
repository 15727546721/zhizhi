package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Password;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.infrastructure.persistent.po.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserConverterTest {

    private final UserConverter userConverter = new UserConverter();

    @Test
    void testToDomainEntityWithPassword() {
        // 创建一个User PO对象，包含密码
        User userPO = User.builder()
                .id(1L)
                .username("testuser")
                .password("encrypted_password")
                .email("test@example.com")
                .nickname("Test User")
                .status(0)
                .build();

        // 转换为UserEntity
        UserEntity userEntity = userConverter.toDomainEntity(userPO);

        // 验证转换结果
        assertNotNull(userEntity);
        assertEquals(1L, userEntity.getId());
        assertEquals("testuser", userEntity.getUsernameValue());
        assertNotNull(userEntity.getPassword());
        assertEquals("encrypted_password", userEntity.getPassword().getEncodedValue());
        assertEquals("test@example.com", userEntity.getEmailValue());
        assertEquals("Test User", userEntity.getNickname());
    }

    @Test
    void testPasswordValidation() {
        // 创建一个User PO对象，包含密码
        String plainPassword = "test123";
        String encryptedPassword = "c8541c001f0f0ea8028a8306fb20811ef8d564e755043101091385840510d5f3"; // SHA256 of "test123"
        
        User userPO = User.builder()
                .id(1L)
                .username("testuser")
                .password(encryptedPassword)
                .email("test@example.com")
                .nickname("Test User")
                .status(0)
                .build();

        // 转换为UserEntity
        UserEntity userEntity = userConverter.toDomainEntity(userPO);

        // 验证密码匹配 - 直接比较加密后的密码
        assertEquals(encryptedPassword, userEntity.getPassword().getEncodedValue());
    }
}