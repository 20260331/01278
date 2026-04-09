package com.fitness.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;

    public PageResult() {}

    public PageResult(IPage<T> page) {
        this.records = page.getRecords();
        this.total = page.getTotal();
        this.size = page.getSize();
        this.current = page.getCurrent();
        this.pages = page.getPages();
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page);
    }
}
