package org.clauseway.logic.goals;

// ABOUTME: An inner solve: a goal run from a package to its seal, settled on entry
// ABOUTME: and on every exit, optionally closed — what a non-monotone read is taken over.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.goals.optimizer.Barrier;
import java.util.List;

/**
 * A sub-search judged after completion. A non-monotone operator — negation, an
 * aggregate, committed choice — reads the ABSENCE of something, and that read
 * is sound only over a sub-search that is partitioned (no parked search crosses
 * it: a {@link Barrier}), complete (read after its seal) and, where the read
 * must not move under later outer bindings, closed (a {@link Watermark}
 * refuses an outside variable inside). An inner solve is a barrier run to the
 * seal; closure is its one switch.
 *
 * <p>Completion is {@link Exhaustion}'s certificate: honest under suspension,
 * since a sub-search that parks at a tabled entry keeps its workforce open
 * until the entry seals, so the read sees a complete answer set, never a
 * partial one.
 */
public final class Subsolve {

	private final Goal goal;
	private final Watermark mark;

	private Subsolve(Goal goal, Watermark mark) {
		this.goal = goal;
		this.mark = mark;
	}

	/** An open inner solve: its bindings may reach outside variables (committed choice). */
	public static Subsolve of(Goal goal) {
		return new Subsolve(goal, null);
	}

	/** Closed under {@code mark}: a variable born before it refuses inside (aggregates). */
	public Subsolve closed(Watermark mark) {
		return new Subsolve(goal, mark);
	}

	/**
	 * Runs from {@code from} to the seal, handing each settled answer package to
	 * {@code consumer} inside the claimed workforce; completes only at the seal.
	 */
	public Fiber<Nothing> each(Knowledge from, Fiber.Fn<Knowledge, Nothing> consumer) {
		return Exhaustion.exhausted(Barrier.of(goal).apply(start(from)).apply(consumer));
	}

	/** Runs from {@code from} to the seal and returns every settled answer package. */
	public Fiber<List<Knowledge>> collect(Knowledge from) {
		return Exhaustion.collected(Barrier.of(goal).apply(start(from)));
	}

	private Knowledge start(Knowledge from) {
		return mark == null ? from : from.putStore(mark);
	}
}
