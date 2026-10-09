// ABOUTME: Aggregation over the solutions of a closed sub-goal: findall and the
// ABOUTME: count/sum/max/min folds, each a Barrier that runs its sub-search to exhaustion.

/**
 * Aggregation goals. {@link org.clauseway.logic.aggregate.Aggregate} is the single
 * entry point: {@code findall}, {@code count}, {@code sum}, {@code max} and
 * {@code min} each hand a body a fresh template variable, run the goal the body
 * builds to exhaustion from the bindings at the aggregate's position, fold the
 * reified answers, and succeed once with the result ({@code max}/{@code min} fail
 * on an empty solution set). Every aggregate is wrapped in an
 * {@link org.clauseway.logic.goals.optimizer.Barrier}, so the goal-tree optimizer
 * does not reorder across it, and runs its sub-search as a closed
 * {@link org.clauseway.logic.constraints.Subsolve} so the fold sees the complete
 * answer set.
 *
 * <p>The sub-goal must be closed: a {@link org.clauseway.logic.goals.Watermark}
 * planted in the sub-solve's {@link org.clauseway.logic.goals.Knowledge} makes a
 * variable born before the aggregate refuse when it surfaces inside the sub-solve.
 * Reification of each answer goes through
 * {@code org.clauseway.logic.constraints.Constraints}.
 */
package org.clauseway.logic.aggregate;
