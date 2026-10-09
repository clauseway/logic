package org.clauseway.logic.constraints;

// ABOUTME: The closed read: a goal run from a package to its seal under a watermark,
// ABOUTME: refusing an answer that still owes a condition — what an aggregate folds over.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.goals.Exhaustion;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.goals.Watermark;
import org.clauseway.logic.goals.optimizer.Barrier;
import java.util.List;

/**
 * A sub-search judged after completion. A non-monotone operator reads the
 * ABSENCE of something, and that read is sound only over a sub-search that is
 * partitioned (no parked search crosses it: a {@link Barrier}), complete (read
 * after the seal {@link Exhaustion} certifies) and closed (a {@link Watermark}
 * refuses an outside variable inside, so later outer bindings cannot move the
 * answer set). This is that sub-search.
 *
 * <p>The enclosing branch's parked suspensions are set aside on entry: inert
 * inside (their variables cannot be bound here), they ripen in the branch that
 * owns them. What is still parked at an exit is therefore the read's own, and
 * such an answer refuses — a read over an answer that owes a condition is a
 * read at zero strength. The open reads — committed choice, the trial — are
 * not this: they continue the branch with the answer and let its conditions
 * ride, through {@link Exhaustion} over a {@link Barrier} directly.
 */
public final class Subsolve {

	private final Goal goal;
	private final Watermark mark;

	private Subsolve(Goal goal, Watermark mark) {
		this.goal = goal;
		this.mark = mark;
	}

	/** The closed read of {@code goal} under {@code mark}. */
	public static Subsolve closed(Goal goal, Watermark mark) {
		return new Subsolve(goal, mark);
	}

	/**
	 * Runs from {@code from} to the seal, handing each answer package to
	 * {@code consumer} inside the claimed workforce; completes only at the seal.
	 */
	public Fiber<Nothing> each(Knowledge from, Fiber.Fn<Knowledge, Nothing> consumer) {
		return Exhaustion.exhausted(Barrier.of(goal).apply(start(from))
				.apply(answer -> consumer.apply(settled(answer))));
	}

	/** Runs from {@code from} to the seal and returns every answer package. */
	public Fiber<List<Knowledge>> collect(Knowledge from) {
		return Exhaustion.collected(Barrier.of(goal).apply(start(from)).map(Subsolve::settled));
	}

	private Knowledge start(Knowledge from) {
		return Propagation.withoutOwed(from).putStore(mark);
	}

	private static Knowledge settled(Knowledge answer) {
		if (Propagation.suspensionsPending(answer)) {
			throw new IllegalStateException(
					"an answer may not be read while it owes a condition: "
							+ "the owed condition cannot ride the answer");
		}
		return answer;
	}
}
