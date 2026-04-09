package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.PageResult;
import com.fitness.common.Result;
import com.fitness.entity.Equipment;
import com.fitness.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 器材管理控制器
 * <p>
 * 管理健身器材信息，包括：
 * - 器材的增删改查
 * - 器材维护记录
 * </p>
 * 
 * <h3>器材状态说明：</h3>
 * <ul>
 *   <li>0 - 报废：器材已报废</li>
 *   <li>1 - 正常：器材正常使用</li>
 *   <li>2 - 维修中：器材正在维修</li>
 * </ul>
 * 
 * @author fitness
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "器材管理", description = "健身器材的增删改查、维护记录等接口")
@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 分页查询器材
     */
    @GetMapping("/page")
    public Result<PageResult<Equipment>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return Result.success(equipmentService.pageList(new Page<>(current, size), name, type, status));
    }

    /**
     * 获取器材详情
     */
    @GetMapping("/{id}")
    public Result<Equipment> getById(@PathVariable Long id) {
        return Result.success(equipmentService.getById(id));
    }

    /**
     * 新增器材
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody Equipment equipment) {
        equipmentService.save(equipment);
        return Result.success();
    }

    /**
     * 修改器材
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody Equipment equipment) {
        equipmentService.updateById(equipment);
        return Result.success();
    }

    /**
     * 删除器材
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.removeById(id);
        return Result.success();
    }

    /**
     * 记录维护
     */
    @Operation(summary = "记录维护", description = "记录器材的维护信息")
    @PutMapping("/{id}/maintenance")
    public Result<Void> maintain(@Parameter(description = "器材ID") @PathVariable Long id, 
                                  @Parameter(description = "维护备注") @RequestParam(required = false) String remark) {
        equipmentService.maintain(id, remark);
        return Result.success();
    }
}
