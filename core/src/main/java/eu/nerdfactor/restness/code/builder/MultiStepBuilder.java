package eu.nerdfactor.restness.code.builder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A container that holds multiple {@link Buildable} build steps.
 *
 * @param <T> The type of {@link Buildable}.
 */
public class MultiStepBuilder<T> {

	/**
	 * A queue of the steps held in the container.
	 */
	protected Queue<Buildable<T>> steps = new LinkedList<>();

	/**
	 * Add a build step to the container.
	 *
	 * @param buildStep A {@link Buildable} to be added.
	 * @return The object in a fluent api pattern.
	 */
	public MultiStepBuilder<T> and(Buildable<T> buildStep) {
		this.steps.add(buildStep);
		return this;
	}

	/**
	 * Get all steps to iterate over them.
	 *
	 * @return An iterator for all steps.
	 */
	public Iterator<Buildable<T>> steps() {
		return steps.iterator();
	}

	/**
	 * Builds all steps with an existing builder.
	 *
	 * @param builder An existing builder object that will be appended.
	 * @return The builder.
	 */
	public T buildAll(T builder) {
		this.steps.forEach(step -> step.buildWith(builder));
		return builder;
	}

}
