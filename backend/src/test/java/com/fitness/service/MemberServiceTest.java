package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.entity.Member;
import com.fitness.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 会员服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("会员服务测试")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("测试分页查询会员")
    void testPageList() {
        Page<Member> page = new Page<>(1, 10);
        PageResult<Member> result = memberService.pageList(page, null, null, null);
        
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("测试按姓名查询会员")
    void testPageListByName() {
        Page<Member> page = new Page<>(1, 10);
        PageResult<Member> result = memberService.pageList(page, "王", null, null);
        
        assertNotNull(result);
        // 验证返回的会员姓名包含"王"
        result.getRecords().forEach(member -> 
            assertTrue(member.getName().contains("王")));
    }

    @Test
    @DisplayName("测试按状态查询会员")
    void testPageListByStatus() {
        Page<Member> page = new Page<>(1, 10);
        PageResult<Member> result = memberService.pageList(page, null, null, 1);
        
        assertNotNull(result);
        // 验证返回的会员状态都是正常
        result.getRecords().forEach(member -> 
            assertEquals(1, member.getStatus()));
    }

    @Test
    @DisplayName("测试获取会员详情")
    void testGetById() {
        Member member = memberService.getById(1L);
        
        assertNotNull(member);
        assertEquals("王小明", member.getName());
        assertEquals(1, member.getGender());
    }

    @Test
    @DisplayName("测试根据用户ID获取会员")
    void testGetByUserId() {
        Member member = memberService.getByUserId(3L);
        
        assertNotNull(member);
        assertEquals("王小明", member.getName());
    }

    @Test
    @DisplayName("测试创建会员")
    void testCreateMember() {
        Member member = new Member();
        member.setName("测试会员");
        member.setGender(1);
        member.setPhone("13800000099");
        member.setLevel(1);

        memberService.createMember(member, "testmember", "123456");

        // 验证会员创建成功
        assertNotNull(member.getId());
        assertNotNull(member.getUserId());
    }

    @Test
    @DisplayName("测试创建会员 - 用户名重复")
    void testCreateMemberWithDuplicateUsername() {
        Member member = new Member();
        member.setName("测试会员2");
        member.setGender(1);
        member.setPhone("13800000098");
        member.setLevel(1);

        // 使用已存在的用户名应该抛出异常
        assertThrows(BusinessException.class, () -> 
            memberService.createMember(member, "admin", "123456"));
    }

    @Test
    @DisplayName("测试更新会员信息")
    void testUpdateMember() {
        Member member = memberService.getById(1L);
        member.setPhone("13900000099");
        
        memberService.updateMember(member);
        
        Member updated = memberService.getById(1L);
        assertEquals("13900000099", updated.getPhone());
    }

    @Test
    @DisplayName("测试更新会员状态")
    void testUpdateStatus() {
        // 冻结会员
        memberService.updateStatus(1L, 0);
        Member frozen = memberService.getById(1L);
        assertEquals(0, frozen.getStatus());
        
        // 恢复会员
        memberService.updateStatus(1L, 1);
        Member active = memberService.getById(1L);
        assertEquals(1, active.getStatus());
    }

    @Test
    @DisplayName("测试获取不存在的会员")
    void testGetNonExistentMember() {
        Member member = memberService.getById(9999L);
        assertNull(member);
    }
}
