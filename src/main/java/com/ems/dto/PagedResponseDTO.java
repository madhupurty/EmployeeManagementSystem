package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * =============================================================================
 * PAGED RESPONSE DTO - Generic wrapper for paginated data
 * =============================================================================
 * 
 * WHAT IS PAGINATION?
 * -------------------
 * Pagination is dividing large datasets into smaller chunks (pages).
 * Instead of returning 10,000 employees at once, return 20 per page.
 * 
 * WHY PAGINATION IS IMPORTANT:
 * 1. PERFORMANCE: Less data transferred over network
 * 2. MEMORY: Client doesn't need to hold all data in memory
 * 3. UX: Users see results faster, can navigate pages
 * 4. DATABASE: Query is faster with LIMIT and OFFSET
 * 
 * PAGINATION PARAMETERS:
 * - page: Current page number (0-indexed in Spring, often 1-indexed in API)
 * - size: Number of items per page (default: 10, 20, or 25)
 * - sort: Field to sort by
 * - direction: ASC or DESC
 * 
 * EXAMPLE API CALLS:
 * GET /api/employees?page=0&size=20&sort=firstName&direction=asc
 * GET /api/employees?page=2&size=10&sort=salary&direction=desc
 * 
 * =============================================================================
 * GENERIC TYPE <T> - Why and How
 * =============================================================================
 * 
 * <T> makes this class reusable for any type of content.
 * 
 * EXAMPLES:
 * - PagedResponseDTO<EmployeeResponseDTO> - Paginated employees
 * - PagedResponseDTO<DepartmentDTO> - Paginated departments (Phase 4)
 * - PagedResponseDTO<LeaveRequestDTO> - Paginated leave requests (Phase 4)
 * 
 * Without generics, we'd need:
 * - EmployeePagedResponse
 * - DepartmentPagedResponse
 * - LeaveRequestPagedResponse
 * 
 * INTERVIEW QUESTION: What are generics and why use them?
 * ANSWER: Generics enable type-safe, reusable code. They allow a single class
 *         to work with multiple types while maintaining compile-time type checking.
 *         This reduces code duplication and catches type errors at compile time.
 * 
 * =============================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {

    /*
     * The actual data items for this page
     * List of T (could be EmployeeResponseDTO, DepartmentDTO, etc.)
     */
    private List<T> content;

    /*
     * Current page number
     * NOTE: Spring Data uses 0-indexed pages internally
     * We expose 1-indexed pages for better UX (page 1, 2, 3 instead of 0, 1, 2)
     */
    private int pageNumber;

    /*
     * Number of items per page
     * Also called "page size" or "limit"
     */
    private int pageSize;

    /*
     * Total number of items across all pages
     * Example: If you have 95 employees with pageSize 20:
     *   totalElements = 95
     *   totalPages = 5 (20 + 20 + 20 + 20 + 15)
     */
    private long totalElements;

    /*
     * Total number of pages
     * Calculated as: ceil(totalElements / pageSize)
     */
    private int totalPages;

    /*
     * Is this the first page?
     * Useful for disabling "Previous" button in UI
     */
    private boolean first;

    /*
     * Is this the last page?
     * Useful for disabling "Next" button in UI
     */
    private boolean last;

    /*
     * Does the current page have content?
     * False if page is beyond total pages
     */
    private boolean hasContent;

    /*
     * Number of items in current page
     * May be less than pageSize on the last page
     * Example: 95 items, pageSize 20, last page has 15 items
     */
    private int numberOfElements;

    // ==========================================================================
    // STATIC FACTORY METHOD - Converts Spring Page to PagedResponseDTO
    // ==========================================================================

    /*
     * Converts Spring Data's Page<T> to our custom PagedResponseDTO<T>
     * 
     * WHY NOT USE Page<T> DIRECTLY IN API RESPONSE?
     * 1. Page interface has many methods we don't need to expose
     * 2. JSON serialization of Page includes implementation details
     * 3. We want control over the exact JSON structure
     * 4. Our DTO uses 1-indexed pages (more intuitive for users)
     * 
     * INTERVIEW QUESTION: What is Spring Data's Page interface?
     * ANSWER: Page<T> is a Spring Data interface that wraps query results
     *         with pagination metadata. It's returned by repository methods
     *         that accept Pageable parameter. It contains content, page number,
     *         total elements, total pages, and navigation helpers.
     */
    public static <T> PagedResponseDTO<T> fromPage(Page<T> page) {
        return PagedResponseDTO.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber() + 1)  // Convert to 1-indexed
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasContent(page.hasContent())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }

    /*
     * Alternative factory method when content needs transformation
     * 
     * Example use case:
     * Page<Employee> employeePage = repository.findAll(pageable);
     * List<EmployeeResponseDTO> dtos = employeePage.getContent()
     *     .stream()
     *     .map(EmployeeResponseDTO::fromEntity)
     *     .toList();
     * PagedResponseDTO<EmployeeResponseDTO> response = 
     *     PagedResponseDTO.fromPage(employeePage, dtos);
     */
    public static <T, U> PagedResponseDTO<U> fromPage(Page<T> page, List<U> transformedContent) {
        return PagedResponseDTO.<U>builder()
                .content(transformedContent)
                .pageNumber(page.getNumber() + 1)  // Convert to 1-indexed
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasContent(page.hasContent())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
