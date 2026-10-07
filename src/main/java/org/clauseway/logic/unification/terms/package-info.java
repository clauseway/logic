// ABOUTME: The term vocabulary: Term as the structural root, Unifiable as solver input,
// ABOUTME: Reified as solver output, Name as the substitution's key type.

/**
 * The term types the unifier operates on.
 * {@link org.clauseway.logic.unification.terms.Term} is the structural root of
 * every logic term. Two capability interfaces refine it:
 * {@link org.clauseway.logic.unification.terms.Unifiable} marks terms that may
 * enter a solver and carries the goal-building {@code unifies} methods, and
 * {@link org.clauseway.logic.unification.terms.Reified} marks terms a solver
 * emits, which cannot re-enter unification.
 * {@link org.clauseway.logic.unification.terms.Name} is what a substitution may
 * bind: a live {@link org.clauseway.logic.unification.terms.LVar} identified by
 * object identity, or a canonical {@link org.clauseway.logic.unification.terms.Any}
 * identified by its number in a reified answer.
 * {@link org.clauseway.logic.unification.terms.LVal} wraps a plain value and is
 * both Unifiable and Reified. The algorithms over these types
 * ({@code walk}, {@code unify}, {@code reify}, {@code instantiate}) live in
 * {@code org.clauseway.logic.unification.MiniKanren} and
 * {@code org.clauseway.logic.unification.Substitutions}; the structural
 * containers that terms may hold live in
 * {@code org.clauseway.logic.unification.structures}.
 */
package org.clauseway.logic.unification.terms;
