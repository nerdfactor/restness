package eu.nerdfactor.restness.code.builder;

import java.util.LinkedList;
import java.util.Queue;

public class MultiStepBuilder<T> {

	protected Queue<Buildable<T>> steps = new LinkedList<>();

	public MultiStepBuilder<T> and(Buildable<T> buildStep) {
		this.steps.add(buildStep);
		return this;
	}

}
