// ABOUTME: Projection goals that wait for deep-groundness: a kernel suspension parks
// ABOUTME: the body until its watched variables are ground, then runs it on the walked values.

/**
 * Suspending projection. {@link org.clauseway.logic.projection.Projection} offers
 * {@code project} over one, two or three variables: each parks a suspension through
 * {@code org.clauseway.logic.constraints.Propagation#suspend} that wakes once every
 * watched variable is deep-ground, then runs the body goal with the walked values;
 * a variable already ground at the call runs the body inline. Each goal declares a
 * {@link org.clauseway.logic.goals.optimizer.Bounded} order of 1 to the goal-tree
 * optimizer.
 *
 * <p>This is the suspending counterpart of {@code org.clauseway.logic.goals.Logic}'s
 * {@code project}, which throws when its variable is unbound at the call. Suspensions
 * themselves belong to {@code org.clauseway.logic.constraints}: this package is a
 * facade over {@code Propagation.suspend} and holds no store.
 */
package org.clauseway.logic.projection;
