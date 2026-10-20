package com.mallcloud.search.controller;

import com.mallcloud.common.core.result.R;
import com.mallcloud.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口：运维用全量重建与索引对账。
 */
@RestController
@RequestMapping("/search/inner")
@RequiredArgsConstructor
public class SearchInnerController {

    private final ProductSearchService searchService;

    @PostMapping("/rebuild")
    public R<Long> rebuild() {
        return R.ok(searchService.rebuildAll());
    }

    @GetMapping("/count")
    public R<Long> count() {
        return R.ok(searchService.count());
    }
}
