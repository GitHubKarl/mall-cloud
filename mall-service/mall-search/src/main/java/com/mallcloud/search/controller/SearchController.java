package com.mallcloud.search.controller;

import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.R;
import com.mallcloud.search.dto.SearchQuery;
import com.mallcloud.search.service.ProductSearchService;
import com.mallcloud.search.vo.SearchItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索前台接口（白名单放行，未登录可搜）。
 */
@Tag(name = "商品搜索")
@RestController
@RequestMapping("/search/public")
@RequiredArgsConstructor
public class SearchController {

    private final ProductSearchService searchService;

    @Operation(summary = "关键词搜索")
    @GetMapping("/query")
    public R<PageResult<SearchItemVO>> query(@Valid SearchQuery query) {
        return R.ok(searchService.search(query));
    }
}
