package cn.xu.domain.like;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class LuaScriptLoader {
    private final ResourceLoader resourceLoader;
    private final RedisScript<Long> likeScript;

    public LuaScriptLoader(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;
        // 从类路径加载 Lua 脚本
        Resource resource = resourceLoader.getResource("classpath:lua/like_script.lua");
        InputStream inputStream = resource.getInputStream();
        String script = new String(IOUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
        this.likeScript = new DefaultRedisScript<>(script, Long.class);
    }

    public RedisScript<Long> getLikeScript() {
        return likeScript;
    }
}
