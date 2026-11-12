package org.clauseway.logic.unification.structures;

// ABOUTME: The structural contract a user's compound value signs to become a term: its members
// ABOUTME: in order, and a rebuild from members; its kind is its class, so only its own kind unifies with it.

import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.stream.StreamSupport;
import org.clauseway.logic.unification.terms.Term;

/**
 * A compound value the unifier takes apart member by member and puts back
 * together: signed explicitly, never inferred from shape. Two signed values
 * unify only when they are the same type, then member by member; the
 * arity is fixed by the type. Every member is a term: a plain value is
 * signed as {@code lval(value)}, the way {@code some(lval(value))} is. A
 * signed value enters the solver as the value of a term, {@code lval(value)},
 * the way an {@code LOption} does.
 */
public interface Structural<R extends Structural<R>> {

	/** The members, one level deep, in a fixed order. */
	Iterable<? extends Term<?>> members();

	/** The same kind rebuilt from members in that order. */
	R withMembers(Term<?>[] members);

	/** This kind with every member a fresh variable: the shape a pattern or a question is minted from. */
	default R fresh() {
		return withMembers(StreamSupport.stream(members().spliterator(), false)
				.map(member -> lvar())
				.toArray(Term<?>[]::new));
	}
}
