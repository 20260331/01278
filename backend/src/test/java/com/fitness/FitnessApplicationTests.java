package com.fitness;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FitnessApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否正常加载
    }
}
