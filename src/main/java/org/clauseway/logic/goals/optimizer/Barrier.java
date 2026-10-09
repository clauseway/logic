package org.clauseway.logic.goals.optimizer;

// ABOUTME: The one explicit boundary: optimize outside and inside, never across.
// ABOUTME: A leaf to every pass; interior structure still optimizes as it unfolds.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.constraints.Propagation;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.Substitutions;
import org.clauseway.vavr.collection.LinkedHashMap;
import java.util.function.ToLongFunction;
import lombok.Value;

/**
 * The explicit form of the contract implicit barriers (impure goals, tabled
 * calls, opaque lambdas) already have: a partition point — it holds its
 * position and nothing reorders across it. The rewriter never enters, so a
 * hand-ordered conjunction inside is never re-sorted; interior defer
 * forcings still consult the ambient {@link OptimizerStore}, so structure
 * that UNFOLDS inside is still optimized. Protect what was written,
 * optimize what unfolds (docs/design/ambient-optimizer.md §5). Execution
 * delegates unchanged.
 */
@Value
public class Barrier implements Goal, Bounded {
	Goal goal;
	ToLongFunction<Knowledge> order;

	private Barrier(Goal goal, ToLongFunction<Knowledge> order) {
		this.goal = goal;
		this.order = order;
	}

	public static Barrier of(Goal goal) {
		return new Barrier(goal, p -> Long.MAX_VALUE);
	}

	/**
	 * A barrier that can price itself against the live state — a tabled call
	 * pricing its completed entry. MAX (the incomplete case) holds position
	 * exactly as an unpriced barrier does; a finite price is the immovability
	 * transition (docs/reference/optimizer.md).
	 */
	public static Barrier priced(ToLongFunction<Knowledge> order, Goal goal) {
		return new Barrier(goal, order);
	}

	@Override
	public long answers(Substitutions s) {
		return order.applyAsLong(Knowledge.of(s, LinkedHashMap.empty()));
	}

	@Override
	public long answers(Knowledge p) {
		return order.applyAsLong(p);
	}

	/**
	 * Nothing parked crosses a barrier in either direction: the branch's pending
	 * search settles before the goal runs, and each emission's settles before it
	 * leaves. Straight through on both sides when nothing is pending.
	 */
	@Override
	public Cont<Knowledge, Nothing> apply(Knowledge s) {
		return Propagation.settleAfterEach(Propagation.settleAndThen(goal).apply(s));
	}

	@Override
	public Fiber<Goal> accept(Optimizer optimizer) {
		return optimizer.visit(this);
	}

	@Override
	public String toString() {
		return "barrier(" + goal + ")";
	}
}
