package org.clauseway.logic.goals.optimizer;

// ABOUTME: A goal with a declared constant order — execution delegates unchanged,
// ABOUTME: the ordering pass reads the price.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.Substitutions;
import org.clauseway.vavr.collection.LinkedHashMap;
import java.util.function.ToLongFunction;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "of")
class BoundedGoal implements Goal, Bounded {
	ToLongFunction<Knowledge> order;
	Goal goal;

	@Override
	public Cont<Knowledge, Nothing> apply(Knowledge s) {
		return goal.apply(s);
	}

	@Override
	public long answers(Substitutions s) {
		// the blind view is a store-less package
		return order.applyAsLong(Knowledge.of(s, LinkedHashMap.empty()));
	}

	@Override
	public long answers(Knowledge p) {
		return order.applyAsLong(p);
	}

	@Override
	public String toString() {
		return goal.toString();
	}
}
