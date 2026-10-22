package org.clauseway.logic.unification.structures;

// ABOUTME: The structural contract a user's compound value signs to become a term: its members
// ABOUTME: in order, and a rebuild from members; its kind is its class, so only its own kind unifies with it.

import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.stream.StreamSupport;

/**
 * A compound value the unifier takes apart member by member and puts back
 * together: signed explicitly, never inferred from shape. Two signed values
 * unify only when they are the same type, then member by member; the
 * arity is fixed by the type. Members are terms, or plain values the
 * engine wraps as such. A signed value enters the solver as the value of
 * a term, {@code lval(value)}, the way an {@code LOption} does.
 */
public interface Structural<R extends Structural<R>> {

	/** The members, one level deep, in a fixed order. */
	Iterable<?> members();

	/** The same kind rebuilt from members in that order. */
	R withMembers(Object[] members);

	/** This kind with every member a fresh variable: the shape a pattern or a question is minted from. */
	default R fresh() {
		return withMembers(StreamSupport.stream(members().spliterator(), false)
				.map(member -> lvar())
				.toArray());
	}
}
