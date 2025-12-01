package cn.xu.cache;


import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Lua脚本加载
 */
@Data
public class LuaScriptManager {
    private static final String LUA_PATH = "lua/";

    private static DefaultRedisScript<Long> loadScript(String fileName) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(LUA_PATH + fileName));
        script.setResultType(Long.class);
        return script;
    }

    public static final DefaultRedisScript<Long> LIKE_ADD_SCRIPT = loadScript("like_add.lua");
    public static final DefaultRedisScript<Long> LIKE_REMOVE_SCRIPT = loadScript("like_remove.lua");

}

