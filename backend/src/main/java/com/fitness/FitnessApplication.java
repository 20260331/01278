package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 健身俱乐部管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.fitness.mapper")
public class FitnessApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessApplication.class, args);
        System.out.println("========================================");
        System.out.println("  健身俱乐部管理系统启动成功!");
        System.out.println("  接口地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
