package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * A simplistic entity wrapper that wraps entities, list and pages of
 * entities.
 *
 * @param <T> The type of the response object.
 * @author Daniel Klug
 */
public class GeneratedEntityWrapper<T> implements DataWrapper<T> {

	private T item = null;
	private List<T> items = null;
	private Pageable page = null;

	@Override
	public void noContent() {
	}

	@Override
	public void setContent(@NotNull T item) {
		this.item = item;
	}

	@Override
	public void setContent(@NotNull List<T> items) {
		this.items = items;
	}

	@Override
	public void setContent(@NotNull Page<T> page) {
		this.items = page.getContent();
		this.page = page.getPageable();
	}

	public @Nullable T getItem() {
		return item;
	}

	public @Nullable List<T> getItems() {
		return items;
	}

	public @Nullable Pageable getPage() {
		return page;
	}
}
