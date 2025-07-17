package com.lxy.gmt_mono;

import com.lxy.gmt_mono.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GmtMonoApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println("------------------- 开始测试 --------------------");
        assertNotNull(userMapper, "UserMapper 没有被注入成功");

        long count = userMapper.selectCount(null);
        System.out.println("g_user 表中的记录数量为：" + count);
        assertTrue(count > 0, "g_user 表中没有测试数据！");
        System.out.println("------------------- 测试结束 --------------------");
    }

}
