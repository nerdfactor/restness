package eu.nerdfactor.restness.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * A simplistic entity wrapper that wraps entities, list and pages of entities.
 *
 * @param <T> The type of the response object.
 * @author Daniel Klug
 */
@Getter
public class RestnessEntityWrapper<T> implements DataWrapper<T> {

	/**
	 * A single item.
	 */
	protected T item = null;

	/**
	 * A list of items.
	 */
	protected List<T> items = null;

	/**
	 * A page of items.
	 */
	protected Pageable page = null;

	/**
	 * Specify that the DataWrapper contains no content.
	 */
	@Override
	public void noContent() {
		this.item = null;
		this.items = null;
		this.page = null;
	}

	/**
	 * Set the content to a single item.
	 *
	 * @param item A single item.
	 */
	@Override
	public void setContent(@NotNull T item) {
		this.noContent();
		this.item = item;
	}

	/**
	 * Set the content to a list of items.
	 *
	 * @param items A list of items.
	 */
	@Override
	public void setContent(@NotNull List<T> items) {
		this.noContent();
		this.items = items;
	}

	/**
	 * Set the content to a page of items.
	 *
	 * @param page A page of items.
	 */
	@Override
	public void setContent(@NotNull Page<T> page) {
		this.noContent();
		this.items = page.getContent();
		this.page = page.getPageable();
	}
}
