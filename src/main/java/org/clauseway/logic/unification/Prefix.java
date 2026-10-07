package org.clauseway.logic.unification;

// ABOUTME: The newly added bindings of one unification — mintable only by the unifier
// ABOUTME: and the checked single-binding constructor, so a prefix is born valid.

import org.clauseway.functional.fibers.MFiber;
import org.clauseway.vavr.Tuple;
import org.clauseway.vavr.Tuple2;
import org.clauseway.vavr.collection.HashMap;
import org.clauseway.vavr.control.Option;
import org.clauseway.logic.unification.terms.LVar;
import org.clauseway.logic.unification.terms.Term;

/**
 * A delta of variable bindings (docs/reference/constraint-kernel.md).
 * Construction is package-private: prefixes come from {@link MiniKanren}'s
 * unification (which walks before extending, so no pair targets a bound variable)
 * or from {@link #binding}, which checks. Consumers revalidate against the live
 * package at application time — a prefix may be applied later than it was minted.
 */
public final class Prefix {

	private final HashMap<LVar<?>, Term<?>> delta;

	Prefix(HashMap<LVar<?>, Term<?>> delta) {
		this.delta = delta;
	}

	/**
	 * A single inferred binding — none when {@code x} is already bound (walk it and
	 * unify instead; asserting over a bound variable is the silent-no-op trap).
	 */
	public static Option<Prefix> binding(Substitutions s, LVar<?> x, Term<?> value) {
		return s.walk(x) == x ?
				Option.of(new Prefix(HashMap.of(x, value))) :
				Option.none();
	}

	public boolean isEmpty() {
		return delta.isEmpty();
	}

	public Iterable<Tuple2<LVar<?>, Term<?>>> bindings() {
		return delta;
	}

	public HashMap<LVar<?>, Term<?>> toMap() {
		return delta;
	}

	/**
	 * The asserted reading of the prefix trichotomy against a live package: each
	 * pair is re-unified over the substitutions as they stand, so a pair whose
	 * variable or value has moved since the mint is read through its current
	 * bindings — a pair for a still-open variable re-targets its walked
	 * representative; one that already holds (both sides walk to the same
	 * variable or value) is dropped; one that clashes is a contradiction — none.
	 * The kept delta is exactly what the unifier added; none is the contradiction.
	 * A fiber, so the caller composes it into its own run instead of nesting an
	 * engine. (A disequality trial reads the same trichotomy with the opposite
	 * polarity — see {@link org.clauseway.logic.constraints.Trial}.)
	 */
	@SuppressWarnings("unchecked")
	public MFiber<Prefix> revalidate(Substitutions s) {
		return delta
				.foldLeft(MFiber.mdone(Tuple.of(s, HashMap.<LVar<?>, Term<?>> empty())),
						(state, binding) -> state.flatMap(st ->
								MiniKanren.unifyPrefix(st._1, (Term<Object>) binding._1, (Term<Object>) binding._2)
										.map(minted -> Tuple.of(minted.appliedTo(st._1), st._2.merge(minted.delta)))))
				.map(st -> new Prefix(st._2));
	}

	/** The substitutions extended with this prefix. */
	public Substitutions appliedTo(Substitutions s) {
		Substitutions result = s;
		for (Tuple2<LVar<?>, Term<?>> binding : delta) {
			result = result.extend(binding._1, binding._2);
		}
		return result;
	}

	/** The substitution map extended with this prefix. */
	public HashMap<LVar<?>, Term<?>> appliedTo(HashMap<LVar<?>, Term<?>> substitutions) {
		HashMap<LVar<?>, Term<?>> result = substitutions;
		for (Tuple2<LVar<?>, Term<?>> binding : delta) {
			result = result.put(binding._1, binding._2);
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Prefix && delta.equals(((Prefix) o).delta);
	}

	@Override
	public int hashCode() {
		return delta.hashCode();
	}

	@Override
	public String toString() {
		return "prefix" + delta;
	}
}
