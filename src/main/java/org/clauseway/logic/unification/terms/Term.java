package org.clauseway.logic.unification.terms;

// ABOUTME: Structural root of all logic terms — values, input variables and reified variables.
// ABOUTME: Walk, unification and reification machinery operate at this level.

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A node in a logic term. Capability interfaces refine it:
 * {@link Unifiable} marks terms that may enter a solver, and reified
 * terms are what a solver emits.
 *
 * @author TGa
 */
public interface Term<T> extends Supplier<T> {

	default Optional<T> asVal() {
		return Optional.empty();
	}

	default boolean isVal() {
		return false;
	}

	default Optional<Name<T>> asName() {
		return Optional.empty();
	}

	default Optional<LVar<T>> asVar() {
		return Optional.empty();
	}

	default Optional<Any<T>> asReified() {
		return Optional.empty();
	}

	@Override
	default T get() {
		return ((LVal<T>) this).getValue();
	}

	default LVar<T> getVar() {
		return (LVar<T>) this;
	}

	@SuppressWarnings("unchecked")
	default Term<Object> getObjectTerm() {
		return (Term<Object>) this;
	}

	/**
	 * This term as something a goal may unify: a variable or a value is one
	 * already, a reified name is not — an answer's free cell has no place in
	 * a goal, and asking refuses.
	 */
	default Unifiable<T> unifiable() {
		throw new IllegalStateException(this + " is a reified name, not a unifiable: an answer's free cell has no place in a goal");
	}
}
