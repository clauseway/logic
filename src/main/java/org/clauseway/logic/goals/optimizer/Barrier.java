package org.clauseway.logic.goals.optimizer;

// ABOUTME: The one explicit boundary: optimize outside and inside, never across.
// ABOUTME: A leaf to every pass; interior structure still optimizes as it unfolds.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.Substitutions;
import org.clauseway.vavr.collection.LinkedHashMap;
import java.util.Optional;
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
	 * Nothing parked crosses a barrier in either direction: the ambient
	 * optimizer discharges what it parked before the goal runs
	 * ({@link Optimizer#entering}) and again behind each emission
	 * ({@link Optimizer#leaving}). Straight through on both sides when there is
	 * no optimizer or it has nothing to do.
	 */
	@Override
	public Cont<Knowledge, Nothing> apply(Knowledge s) {
		return settleAfterEach(settleAndThen(goal).apply(s));
	}

	/** The goal behind an entry: the optimizer discharges first, then the goal runs on each child. */
	public static Goal settleAndThen(Goal goal) {
		return s -> OptimizerStore.from(s)
				.map(store -> store.entering(s))
				.getOrElse(Optional.empty())
				.map(discharged -> discharged.flatMap(goal))
				.orElseGet(() -> goal.apply(s));
	}

	/** The emissions leaving: the optimizer discharges on each before it is handed on. */
	public static Cont<Knowledge, Nothing> settleAfterEach(Cont<Knowledge, Nothing> emissions) {
		return k -> emissions.apply(p -> OptimizerStore.from(p)
				.map(store -> store.leaving(p))
				.getOrElse(Optional.empty())
				.map(discharged -> discharged.apply(k))
				.orElseGet(() -> k.apply(p)));
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
