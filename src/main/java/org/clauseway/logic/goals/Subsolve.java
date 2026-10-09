package org.clauseway.logic.goals;

// ABOUTME: An inner solve: a goal run from a package to its seal, settled on entry
// ABOUTME: and on every exit, optionally closed — what a non-monotone read is taken over.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.constraints.Propagation;
import org.clauseway.logic.constraints.store.Suspension;
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
	private final boolean deliversOwed;

	private Subsolve(Goal goal, Watermark mark, boolean deliversOwed) {
		this.goal = goal;
		this.mark = mark;
		this.deliversOwed = deliversOwed;
	}

	/**
	 * An open inner solve: its bindings may reach outside variables (committed
	 * choice). At each exit an owed condition born inside refuses — a read over
	 * answers that still owe a condition is a read at zero strength.
	 */
	public static Subsolve of(Goal goal) {
		return new Subsolve(goal, null, false);
	}

	/** Closed under {@code mark}: a variable born before it refuses inside (aggregates). */
	public Subsolve closed(Watermark mark) {
		return new Subsolve(goal, mark, deliversOwed);
	}

	/**
	 * An owed condition born inside is delivered as part of the answer instead
	 * of refused: the trial's reading, where an undecided world is a result.
	 */
	public Subsolve deliveringOwed() {
		return new Subsolve(goal, mark, true);
	}

	/**
	 * Runs from {@code from} to the seal, handing each settled answer package to
	 * {@code consumer} inside the claimed workforce; completes only at the seal.
	 */
	public Fiber<Nothing> each(Knowledge from, Fiber.Fn<Knowledge, Nothing> consumer) {
		long since = Suspension.births();
		return Exhaustion.exhausted(Barrier.of(goal).apply(start(from))
				.apply(answer -> consumer.apply(settled(answer, since))));
	}

	/** Runs from {@code from} to the seal and returns every settled answer package. */
	public Fiber<List<Knowledge>> collect(Knowledge from) {
		long since = Suspension.births();
		return Exhaustion.collected(Barrier.of(goal).apply(start(from)).map(answer -> settled(answer, since)));
	}

	private Knowledge start(Knowledge from) {
		return mark == null ? from : from.putStore(mark);
	}

	private Knowledge settled(Knowledge answer, long since) {
		if (!deliversOwed && Propagation.owed(answer, since)) {
			throw new IllegalStateException(
					"an answer may not be read while it owes a condition born inside the read: "
							+ "the owed condition cannot ride the answer");
		}
		return answer;
	}
}
