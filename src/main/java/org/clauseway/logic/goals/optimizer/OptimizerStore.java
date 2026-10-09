package org.clauseway.logic.goals.optimizer;

// ABOUTME: The ambient optimizer riding the Knowledge (DebugStore pattern): state
// ABOUTME: flows through defer walls, so the pass is waiting when bodies unfold.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.goals.Packaged;
import org.clauseway.vavr.control.Option;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Carries the optimizer pipeline with the solver state
 * (docs/design/ambient-optimizer.md). Seeded by {@link org.clauseway.logic.solving.Query#optimized(Optimizer)};
 * consulted at exactly one hook — {@link Goal#defer} forcing — so freshly
 * materialized recursion layers are rewritten against live bindings.
 */
@Value
@RequiredArgsConstructor(staticName = "of")
public class OptimizerStore implements Packaged {
	Optimizer pipeline;

	public static Option<OptimizerStore> from(Knowledge pkg) {
		return pkg.getStores().get(OptimizerStore.class).map(OptimizerStore.class::cast);
	}

	public Fiber<Goal> rewrite(Goal body, Knowledge p) {
		return body.accept(pipeline.with(p));
	}

	/** A barrier is entered with {@code p}: the pipeline discharges what it parked. */
	public Optional<Cont<Knowledge, Nothing>> entering(Knowledge p) {
		return pipeline.entering(p);
	}

	/** An answer leaves a barrier as {@code p}: the pipeline discharges what it parked. */
	public Optional<Cont<Knowledge, Nothing>> leaving(Knowledge p) {
		return pipeline.leaving(p);
	}
}
