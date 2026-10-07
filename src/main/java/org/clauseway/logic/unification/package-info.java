// ABOUTME: The unifier and the substitution it extends: walk, unify, occurs check,
// ABOUTME: reification, and the Prefix delta that constraint-aware code applies.

/**
 * The unification core. {@link org.clauseway.logic.unification.MiniKanren} holds
 * the static algorithms over terms: {@code unify} and {@code unifyPrefix} (the
 * occurs-checked unifier, with and without applying the result), {@code walkAll},
 * {@code reify} and {@code instantiate} (the conversion between solver terms and
 * reified answers), and {@code members} (the one-level structural decomposition
 * of tuples, {@code LList} and {@code LTree}).
 * {@link org.clauseway.logic.unification.Substitutions} is the read-only binding
 * map every algorithm is typed over, ordered as a semilattice whose join is
 * unification; its hashed representation is package-private.
 * {@link org.clauseway.logic.unification.Prefix} is the delta of bindings one
 * unification adds, mintable only here, and the only form in which
 * {@code org.clauseway.logic.constraints.Propagation} lets substitutions grow.
 * The term types this package operates on live in
 * {@code org.clauseway.logic.unification.terms}; the structural containers in
 * {@code org.clauseway.logic.unification.structures}; the goal-building
 * entry points over unification are in {@code org.clauseway.logic.constraints}
 * and {@code org.clauseway.logic.goals}.
 */
package org.clauseway.logic.unification;
