package org.clauseway.logic.goals.optimizer;

// ABOUTME: The parking pass's state in the Knowledge: the disjunctions it took off
// ABOUTME: their textual position, forked at the next barrier.

import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.goals.Packaged;
import org.clauseway.vavr.collection.List;
import lombok.Value;

/**
 * Persistent and branch-local, as every package citizen is: each fork keeps its
 * own list, so a disjunction parked before a fork is owed by every child.
 * Absent means empty.
 */
@Value
public class Parked implements Packaged {
	static final Parked EMPTY = new Parked(List.empty());

	List<Goal> disjunctions;

	public static Parked in(Knowledge p) {
		return p.getStores().get(Parked.class).map(Parked.class::cast).getOrElse(EMPTY);
	}

	public Parked park(Goal disjunction) {
		return new Parked(disjunctions.append(disjunction));
	}

	public boolean isEmpty() {
		return disjunctions.isEmpty();
	}

	@Override
	public String toString() {
		return "parked" + disjunctions;
	}
}
