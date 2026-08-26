package com.terralink.data.model;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> items;
    private int page;
    private int pageSize;
    private int totalCount;
    private int totalPages;

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
