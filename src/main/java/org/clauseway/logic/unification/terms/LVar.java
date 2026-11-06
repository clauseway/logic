package org.clauseway.logic.unification.terms;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import java.util.Optional;

/**
 * @author TGa
 */

public class LVar<T> implements Unifiable<T>, Name<T> {
	private static final AtomicLong VARIABLE_COUNTER = new AtomicLong(0L);
	@Getter
	private final long birth;
	private final String name;

	private LVar(String name) {
		this.birth = VARIABLE_COUNTER.getAndIncrement();
		this.name = name;
	}

	LVar() {
		this.birth = VARIABLE_COUNTER.getAndIncrement();
		this.name = null;
	}

	/** A fresh variable standing for this one: its own birth, the same name when this one was given one. */
	public LVar<T> fresh() {
		return name == null ? new LVar<>() : new LVar<>(name);
	}

	/** The birth counter's current value: every variable created from now on satisfies {@code getBirth() >= births()}. */
	public static long births() {
		return VARIABLE_COUNTER.get();
	}

	public static <T> Unifiable<T> lvar() {
		return new LVar<>();
	}

	public static <T> Unifiable<T> lvar(String name) {
		return new LVar<>(name);
	}

	@Override
	public Optional<Name<T>> asName() {
		return Optional.of(this);
	}

	@Override
	public Optional<LVar<T>> asVar() {
		return Optional.of(this);
	}

	/** The name this variable renders by: the one it was given, or {@code _.birth}. */
	public String getName() {
		return name == null ? "_." + birth : name;
	}

	@Override
	public String toString() {
		return "<" + getName() + ">";
	}
}
