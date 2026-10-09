package org.clauseway.logic.goals.optimizer;

// ABOUTME: Never fork while determinate work remains: every Conde parks instead of
// ABOUTME: forking, and the pass forks what it parked when a barrier is crossed.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import java.util.Optional;

/**
 * The Basic Andorra Model seated at the barrier
 * (docs/notes/conde-parks-until-enforce.md): a {@link Conde} reached inside a
 * conjunction does not fork at its textual position. It becomes a
 * {@link Parking} that records the disjunction in the package's {@link Parked}
 * and succeeds; postings resolve, propagators cascade, suspensions ripen,
 * bodies unfold. When a {@link Barrier} is crossed — the answer leaving, a
 * tabled call, committed choice, an aggregate — the pass forks the parked
 * disjunctions one at a time, each child crossing again, until none is left.
 * Every fork therefore sees the fixpoint of every determinate conjunct in the
 * branch, wherever it was written.
 *
 * <p>Alternatives are rewritten before the disjunction is parked, so a nested
 * {@code Conde} parks in its turn when its alternative runs. The pass never
 * enters a {@link Barrier}, committed choice or an unrecognised goal: a
 * disjunction written inside one forks eagerly, as written.
 */
public class ParkingOptimizer implements Optimizer {

	@Override
	public Goal visit(Conde conde) {
		return new Parking(Conde.of(Optimizer.visitAll(conde.getClauses(), g -> g.accept(this))));
	}

	@Override
	public Optional<Cont<Knowledge, Nothing>> entering(Knowledge p) {
		return discharge(p);
	}

	@Override
	public Optional<Cont<Knowledge, Nothing>> leaving(Knowledge p) {
		return discharge(p);
	}

	/** Forks the first parked disjunction; each child discharges again until nothing is parked. */
	private Optional<Cont<Knowledge, Nothing>> discharge(Knowledge p) {
		Parked parked = Parked.in(p);
		if (parked.isEmpty()) {
			return Optional.empty();
		}
		Goal first = parked.getDisjunctions().head();
		Knowledge rest = p.putStore(new Parked(parked.getDisjunctions().tail()));
		return Optional.of(first.apply(rest)
				.flatMap(child -> discharge(child).orElseGet(() -> Cont.just(child))));
	}
}
