package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A generic wrapper for response objects.
 *
 * @param <T> The type of the response object.
 * @author Daniel Klug
 */
public interface DataWrapper<T> {

	/**
	 * Specify that the DataWrapper contains no content.
	 */
	void noContent();

	/**
	 * Set the content to a single item.
	 */
	void setContent(@NotNull T item);

	/**
	 * Set the content to a list of items.
	 */
	void setContent(@NotNull List<T> items);

	/**
	 * Set the content to a page of items.
	 */
	void setContent(@NotNull Page<T> page);
}
