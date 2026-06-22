package nst.wms.user.internal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Generic paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items")
    private List<T> data;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Items per page", example = "20")
    private int size;

    @Schema(description = "Total number of items", example = "100")
    private long count;

    @Schema(description = "Total number of pages", example = "5")
    private int pages;

    public PageResponse() {
    }

    public PageResponse(List<T> data, int page, int size, long count, int pages) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.count = count;
        this.pages = pages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
