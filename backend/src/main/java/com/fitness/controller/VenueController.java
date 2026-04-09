package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Venue;
import com.fitness.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 场地管理控制器
 * <p>
 * 管理健身场地信息，包括：
 * - 场地的增删改查
 * - 场地状态管理
 * </p>
 * 
 * <h3>场地状态说明：</h3>
 * <ul>
 *   <li>0 - 停用：场地暂停使用</li>
 *   <li>1 - 正常：场地正常使用中</li>
 *   <li>2 - 维护中：场地正在维护</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "场地管理", description = "健身场地的增删改查、状态管理等接口")
@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VenueController {

    private final VenueService venueService;

    /**
     * 分页查询场地
     */
    @GetMapping("/page")
    public Result<PageResult<Venue>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return Result.success(venueService.pageList(new Page<>(current, size), name, type, status));
    }

    /**
     * 获取场地详情
     */
    @GetMapping("/{id}")
    public Result<Venue> getById(@PathVariable Long id) {
        return Result.success(venueService.getById(id));
    }

    /**
     * 新增场地
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody Venue venue) {
        venueService.save(venue);
        return Result.success();
    }

    /**
     * 修改场地
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody Venue venue) {
        venueService.updateById(venue);
        return Result.success();
    }

    /**
     * 删除场地
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        venueService.removeById(id);
        return Result.success();
    }

    /**
     * 修改场地状态
     */
    @Operation(summary = "修改场地状态", description = "修改场地的使用状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "场地ID") @PathVariable Long id, 
                                      @Parameter(description = "状态：0-停用 1-正常 2-维护中") @RequestParam Integer status) {
        Venue venue = new Venue();
        venue.setId(id);
        venue.setStatus(status);
        venueService.updateById(venue);
        return Result.success();
    }
}
