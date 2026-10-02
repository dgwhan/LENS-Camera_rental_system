package com.lens.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Reusable Pagination Configuration and Utility.
 *
 * @author Duong Ngoc Han
 */
@Named(value = "paginationConfig")
@ApplicationScoped
public class PaginationConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_PAGE_SIZE_PARAM = "DEFAULT_PAGE_SIZE";

    public PaginationConfig() {
    }

    /**
     * Resolves default page size from web.xml context-param (DEFAULT_PAGE_SIZE).
     *
     * @return configured page size, or 0 if not found / invalid
     */
    public static int getDefaultPageSize() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext == null || facesContext.getExternalContext() == null) {
                return 0;
            }

            String value = facesContext.getExternalContext().getInitParameter(DEFAULT_PAGE_SIZE_PARAM);
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }

            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Resolves requested page size against default page size.
     *
     * @param requestedPageSize requested page size (e.g. from viewParam)
     * @return valid page size (> 0) or default page size
     */
    public static int resolvePageSize(int requestedPageSize) {
        if (requestedPageSize > 0) {
            return requestedPageSize;
        }
        return getDefaultPageSize();
    }

    /**
     * Calculates total pages given total items and page size.
     *
     * @param totalItems total number of items
     * @param pageSize number of items per page
     * @return total pages (minimum 1)
     */
    public static int calculateTotalPages(int totalItems, int pageSize) {
        if (pageSize <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    }

    /**
     * Clamps current page to valid range [1, totalPages].
     *
     * @param page current requested page
     * @param totalPages total pages available
     * @return clamped page number
     */
    public static int clampPage(int page, int totalPages) {
        if (page < 1) {
            return 1;
        }
        int maxPages = Math.max(1, totalPages);
        if (page > maxPages) {
            return maxPages;
        }
        return page;
    }

    /**
     * Slices a list into the requested page.
     *
     * @param <T> element type
     * @param items full list of items
     * @param page current page (1-based)
     * @param pageSize items per page
     * @return sublist for current page
     */
    public static <T> List<T> paginate(List<T> items, int page, int pageSize) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        if (pageSize <= 0) {
            return items;
        }
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        if (fromIndex >= items.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return items.subList(fromIndex, toIndex);
    }

    /**
     * Instance getter for JSF EL expressions (e.g. #{paginationConfig.pageSize}).
     *
     * @return default page size
     */
    public int getPageSize() {
        return getDefaultPageSize();
    }
}
