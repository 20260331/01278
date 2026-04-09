package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.LoginDTO;
import com.fitness.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 会员管理接口测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("会员管理接口测试")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String memberToken;
    private String coachToken;

    @BeforeEach
    void setUp() throws Exception {
        // 获取管理员token
        adminToken = getToken("admin", "123456");
        // 获取会员token
        memberToken = getToken("member1", "123456");
        // 获取教练token
        coachToken = getToken("coach1", "123456");
    }

    private String getToken(String username, String password) throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"token\":\"") + 9;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    @Test
    @DisplayName("测试分页查询会员 - 管理员权限")
    void testPageMembersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/members/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("测试分页查询会员 - 教练权限")
    void testPageMembersAsCoach() throws Exception {
        mockMvc.perform(get("/api/v1/members/page")
                        .header("Authorization", "Bearer " + coachToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试分页查询会员 - 会员无权限")
    void testPageMembersAsMember() throws Exception {
        mockMvc.perform(get("/api/v1/members/page")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试按姓名筛选会员")
    void testPageMembersWithNameFilter() throws Exception {
        mockMvc.perform(get("/api/v1/members/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("name", "王"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取会员详情")
    void testGetMemberById() throws Exception {
        mockMvc.perform(get("/api/v1/members/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("王小明"));
    }

    @Test
    @DisplayName("测试获取当前会员信息")
    void testGetCurrentMember() throws Exception {
        mockMvc.perform(get("/api/v1/members/current")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("王小明"));
    }

    @Test
    @DisplayName("测试新增会员")
    void testCreateMember() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "newmember");
        params.put("password", "123456");
        params.put("name", "新会员");
        params.put("gender", 1);
        params.put("phone", "13800138000");
        params.put("level", 1);

        mockMvc.perform(post("/api/v1/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试新增会员 - 非管理员无权限")
    void testCreateMemberAsNonAdmin() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "newmember2");
        params.put("password", "123456");
        params.put("name", "新会员2");
        params.put("gender", 1);
        params.put("phone", "13800138001");
        params.put("level", 1);

        mockMvc.perform(post("/api/v1/members")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试会员修改个人信息 - 会员权限")
    void testUpdateMemberProfileAsMember() throws Exception {
        // 先获取当前会员信息
        MvcResult currentResult = mockMvc.perform(get("/api/v1/members/current")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andReturn();
        
        // 构建修改请求
        Member updateMember = new Member();
        updateMember.setId(1L);
        updateMember.setName("王小明修改");
        updateMember.setGender(1);
        updateMember.setPhone("13900139001");

        mockMvc.perform(put("/api/v1/members")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 恢复原始数据
        updateMember.setName("王小明");
        updateMember.setPhone("13800138001");
        mockMvc.perform(put("/api/v1/members")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMember)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试管理员修改会员信息")
    void testUpdateMemberProfileAsAdmin() throws Exception {
        Member updateMember = new Member();
        updateMember.setId(1L);
        updateMember.setName("王小明测试");
        updateMember.setGender(1);
        updateMember.setPhone("13900139002");
        updateMember.setLevel(2);

        mockMvc.perform(put("/api/v1/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 恢复原始数据
        updateMember.setName("王小明");
        updateMember.setPhone("13800138001");
        updateMember.setLevel(1);
        mockMvc.perform(put("/api/v1/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMember)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试修改会员状态")
    void testUpdateMemberStatus() throws Exception {
        mockMvc.perform(put("/api/v1/members/status/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 恢复状态
        mockMvc.perform(put("/api/v1/members/status/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试未授权访问")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/members/page"))
                .andExpect(status().isUnauthorized());
    }
}
