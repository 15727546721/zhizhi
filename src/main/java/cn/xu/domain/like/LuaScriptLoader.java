package cn.xu.domain.like;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;


@Component
public class LuaScriptLoader {

    public RedisScript<Long> getLikeScript(String scriptPath) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(scriptPath)));
        // 返回值类型
        script.setResultType(Long.class);
        return script;
    }
}
