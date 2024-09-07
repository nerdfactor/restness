package eu.nerdfactor.restness.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper for Page in order to provide Page return value to
 * search controller endpoints.
 *
 * @author Daniel Klug
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
public class DataPage<T> extends PageImpl<T> {

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public DataPage(@JsonProperty("content") List<T> content,
	                @JsonProperty("number") int page,
	                @JsonProperty("size") int size,
	                @JsonProperty("totalElements") long total) {
		super(content, Pageable.ofSize(Math.max(1, size)).withPage(page), total);
	}

	public DataPage(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public DataPage(List<T> content) {
		super(content);
	}

	public DataPage() {
		super(new ArrayList());
	}
}