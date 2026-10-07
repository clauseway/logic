// ABOUTME: Goal-tree rewriting before execution: the Optimizer visitor, its shipped passes,
// ABOUTME: and the Bounded/Barrier vocabulary the passes read to price and partition goals.

/**
 * Rewrites goal trees before they run. {@link org.clauseway.logic.goals.optimizer.Optimizer}
 * is the visitor every pass implements and {@code Optimizer.pipeline} composes passes in
 * order; the shipped passes are {@link org.clauseway.logic.goals.optimizer.CascadingOptimizer}
 * (flattens nested conjunctions and disjunctions),
 * {@link org.clauseway.logic.goals.optimizer.OrderingOptimizer} (sorts conjunction
 * segments by declared answer bound) and
 * {@link org.clauseway.logic.goals.optimizer.DoomPruner} (folds refuted postings to failure).
 * {@link org.clauseway.logic.goals.optimizer.Bounded} is the capability a goal implements to
 * declare how many answers it may emit, and
 * {@link org.clauseway.logic.goals.optimizer.Barrier} marks a goal that holds its position
 * and is never entered by a pass.
 *
 * <p>{@link org.clauseway.logic.goals.optimizer.OptimizerStore} carries the pipeline inside
 * the {@link org.clauseway.logic.goals.Knowledge} as a
 * {@link org.clauseway.logic.goals.Packaged} store; it is seeded by
 * {@code org.clauseway.logic.solving.Query#optimized} and consulted when
 * {@link org.clauseway.logic.goals.Goal#defer} forces a deferred goal, so each recursion
 * layer is rewritten against the live state.
 */
package org.clauseway.logic.goals.optimizer;
