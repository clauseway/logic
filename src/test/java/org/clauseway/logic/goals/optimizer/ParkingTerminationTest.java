package org.clauseway.logic.goals.optimizer;

// ABOUTME: Pins parking's one termination hazard: a conde-free recursion that stops
// ABOUTME: only through a fork's binding diverges parked, and Barrier is the remedy.

import org.clauseway.logic.solving.Query;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.interpreter.Scope;
import org.clauseway.functional.fibers.interpreter.StepListener;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

/**
 * Exposure (iii) of docs/notes/conde-parks-until-enforce.md: eager, the fork
 * binds {@code x} and the loop's head unification fails at once; parked, the
 * loop runs with {@code x} open, binds it to {@code f(y)}, and unfolds forever
 * — no barrier is reached because the determinate work never ends. A
 * recursion whose recursive call sits inside an alternative is not in this
 * class: each unfolding parks and returns.
 */
public class ParkingTerminationTest {

	private static final long CAP = 20_000;

	/** {@code loop(x) :- x ≡ f(y) ∧ loop(y)} — conde-free, unfolding through defer. */
	static Goal loop(Unifiable<Object> x) {
		Unifiable<Object> y = lvar();
		return x.unifies(lval(Tuple.of(y))).and(Goal.defer(() -> loop(y)));
	}

	/** {@code loop(x) :- x ≡ nil ∨ (x ≡ f(y) ∧ loop(y))} — the base case is an alternative. */
	static Goal loopWithBase(Unifiable<Object> x) {
		Unifiable<Object> y = lvar();
		return x.unifies(lval("nil")).or(x.unifies(lval(Tuple.of(y))).and(Goal.defer(() -> loopWithBase(y))));
	}

	static Goal choice(Unifiable<Object> x) {
		return x.unifies(lval("a")).or(x.unifies(lval("b")));
	}

	/** Answers under the fair driver, refusing past {@code CAP} steps. */
	private static long cappedAnswers(Query query, Unifiable<?> out) {
		AtomicLong steps = new AtomicLong();
		StepListener capping = new StepListener() {
			@Override
			public void onStep(Fiber<?> node, Scope scope, String name) {
				if (steps.incrementAndGet() > CAP) {
					throw new IllegalStateException("capped at " + CAP + " steps");
				}
			}
		};
		return query.on(fiber -> new BreadthFirstScheduler<>(fiber).withListener(capping)).solve(out).count();
	}

	@Test
	public void eagerTheForkBindsFirstAndTheLoopFailsAtItsHead() {
		Unifiable<Object> x = lvar();

		assertThat(cappedAnswers(Query.of(choice(x).and(loop(x))), x)).isZero();
	}

	@Test
	public void parkedTheLoopRunsOpenAndNeverReachesABarrier() {
		Unifiable<Object> x = lvar();

		assertThatThrownBy(() -> cappedAnswers(Query.of(choice(x).and(loop(x))).optimized(new ParkingOptimizer()), x))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("capped");
	}

	@Test
	public void aBarrierOnTheLoopForksFirst() {
		Unifiable<Object> x = lvar();

		assertThat(cappedAnswers(Query.of(choice(x).and(Barrier.of(loop(x)))).optimized(new ParkingOptimizer()), x)).isZero();
	}

	@Test
	public void aLoopWhoseBaseCaseIsAnAlternativeParksItself() {
		Unifiable<Object> x = lvar();

		assertThat(cappedAnswers(Query.of(choice(x).and(loopWithBase(x))).optimized(new ParkingOptimizer()), x)).isZero();
	}
}
