// ABOUTME: The goal layer: Goal (Knowledge -> Cont) and its combinators, the immutable
// ABOUTME: Knowledge state with its Packaged stores, and the relational standard library.

/**
 * The goal layer. {@link org.clauseway.logic.goals.Goal} is the central interface — a
 * function from a {@link org.clauseway.logic.goals.Knowledge} to a continuation that
 * delivers each resulting state and stays silent on failure — with its combinators
 * {@link org.clauseway.logic.goals.Conjunction}, {@link org.clauseway.logic.goals.Conde},
 * {@link org.clauseway.logic.goals.Conda}, {@link org.clauseway.logic.goals.Condu} and
 * {@link org.clauseway.logic.goals.NamedGoal} (the tracing and profiling hook).
 * {@link org.clauseway.logic.goals.Knowledge} is the immutable solver state: the
 * substitutions plus {@link org.clauseway.logic.goals.Packaged} stores keyed by class,
 * which is how the tracer, the tables, the optimizer and
 * {@link org.clauseway.logic.goals.Watermark} are carried through the search.
 * {@link org.clauseway.logic.goals.Logic} and {@link org.clauseway.logic.goals.Matche}
 * are the relational standard library (list relations, {@code exist}, {@code project},
 * pattern-matching cases); {@link org.clauseway.logic.goals.Exhaustion} certifies that a
 * sub-search has delivered its complete answer set; {@link org.clauseway.logic.goals.GoalSemirings}
 * states the semiring laws the goal algebra satisfies.
 *
 * <p>A goal is run only through {@code org.clauseway.logic.solving.Query}; unification
 * and terms live in {@code org.clauseway.logic.unification}, constraint-aware goals in
 * {@code org.clauseway.logic.constraints}, and goal-tree rewriting in
 * {@code org.clauseway.logic.goals.optimizer}.
 */
package org.clauseway.logic.goals;
