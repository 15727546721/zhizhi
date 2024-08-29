package cn.xu.test.infrastructure.persistent;

import cn.xu.infrastructure.persistent.dao.IUserDao;
import cn.xu.infrastructure.persistent.po.UserPO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 单元测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTest {

    @Resource
    private IUserDao iUserserDao;

    @Test
    public void test() {
        iUserserDao.insert(new UserPO(10001L, "小傅哥", "fustack", "123456", "13800138000", "fustack@qq.com", "1", "1", new Date(), new Date()));
    }


    /**
     * 路由测试
     */
//    @Test
//    public void test_idx() {
//        for (int i = 0; i < 50; i++) {
//            String user_id = "xfg_" + RandomStringUtils.randomAlphabetic(6);
//            log.info("测试结果 {}", (user_id.hashCode() ^ (user_id.hashCode()) >>> 16) & 3);
//        }
//    }

}
