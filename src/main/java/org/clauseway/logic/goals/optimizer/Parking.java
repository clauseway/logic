package org.clauseway.logic.goals.optimizer;

// ABOUTME: A disjunction taken off its textual position: applied, it parks itself in
// ABOUTME: the package and succeeds; the next barrier forks it.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import lombok.Value;

/**
 * What {@link ParkingOptimizer} rewrites a {@code Conde} into. The branch goes
 * on with everything determinate; the fork happens when a {@link Barrier} is
 * crossed, in the knowledge every determinate conjunct has contributed by
 * then.
 */
@Value
public class Parking implements Goal {
	Goal disjunction;

	@Override
	public Cont<Knowledge, Nothing> apply(Knowledge s) {
		return Cont.just(s.putStore(Parked.in(s).park(disjunction)));
	}

	@Override
	public String toString() {
		return "parking(" + disjunction + ")";
	}
}
