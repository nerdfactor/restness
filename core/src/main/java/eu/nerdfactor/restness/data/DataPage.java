package eu.nerdfactor.restness.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper for a {@link Page} in order to provide {@link Page} return
 * value to search controller endpoints.
 *
 * @author Daniel Klug
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
public class DataPage<T> extends PageImpl<T> {

	/**
	 * Construct a {@code DataPage}.
	 *
	 * @param content The content of the page.
	 * @param page    The number of the current page.
	 * @param size    The size of the current page.
	 * @param total   The amount of total elements.
	 */
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public DataPage(@JsonProperty("content") List<T> content,
	                @JsonProperty("number") int page,
	                @JsonProperty("size") int size,
	                @JsonProperty("totalElements") long total) {
		super(content, Pageable.ofSize(Math.max(1, size)).withPage(page), total);
	}

	/**
	 * Construct a {@code DataPage}.
	 *
	 * @param content  The content of the page.
	 * @param pageable The paging information.
	 * @param total    The amount of total elements.
	 */
	public DataPage(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	/**
	 * Construct a {@code DataPage}. The paging information will be inferred
	 * from the list size.
	 *
	 * @param content The content of the page.
	 */
	public DataPage(List<T> content) {
		super(content);
	}

	/**
	 * Construct an empty {@code DataPage}.
	 */
	public DataPage() {
		super(new ArrayList<>());
	}
}