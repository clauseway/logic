// ABOUTME: Semiring-weighted search: per-branch weights carried in the package,
// ABOUTME: folded over answers, and star-solved through tabled recursion.

/**
 * Weighted inference over semirings from {@code org.clauseway.functional.algebra}.
 * {@link org.clauseway.logic.weight.Weights} is the entry point:
 * {@code Weights.factor(ring, weight)} is a goal that multiplies a weight into
 * the running value of its ring, and {@code solve}, {@code solveEach},
 * {@code solveBounded} and {@code solveClosed} run a goal and fold or stream
 * the per-answer weights. {@link org.clauseway.logic.weight.SemiringStore} is
 * the {@code Packaged} value that carries one running value per
 * participating ring; its {@code product} builds the componentwise semiring
 * the solves operate on, and its {@code table} methods build the tabling
 * {@code Table} for a bounded or closed ring
 * ({@code org.clauseway.logic.solving.Query#weighted} seeds both). Closed
 * (star) tabling collects a dependency graph of
 * {@link org.clauseway.logic.weight.Node}s and
 * {@link org.clauseway.logic.weight.Edge}s while tabled calls are open
 * ({@link org.clauseway.logic.weight.Recurrent} and
 * {@link org.clauseway.logic.weight.Fragment} tag the packages involved) and
 * {@link org.clauseway.logic.weight.StarSolve} solves the resulting linear
 * system at seal time. The master/consumer skeleton the closed mode plugs
 * into is {@code org.clauseway.logic.tabling.Tabling}.
 */
package org.clauseway.logic.weight;
