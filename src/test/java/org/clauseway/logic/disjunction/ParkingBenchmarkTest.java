package org.clauseway.logic.disjunction;

// ABOUTME: Parking's pins: do no harm on the ratified lanes, make conjunct order
// ABOUTME: irrelevant, and stay a constant factor from the best hand order.

import org.clauseway.logic.solving.Query;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.interpreter.Scope;
import org.clauseway.functional.fibers.interpreter.StepListener;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.functional.fibers.schedulers.DepthFirstScheduler;
import org.clauseway.logic.TestSchedulers;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Logic;
import org.clauseway.logic.goals.optimizer.Optimizer;
import org.clauseway.logic.goals.optimizer.ParkingOptimizer;
import org.clauseway.logic.unification.structures.LList;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 * The parking pass measured against the lanes {@link SchedulingBenchmarkTest}
 * ratified and against the note's R2 (docs/notes/conde-parks-until-enforce.md):
 * {@code p ∧ q} with {@code p} a disjunction behind a {@code defer} and
 * {@code q} determinate only after unfolding, the determinacy no rewriter can
 * read off the tree. Step counts are exact under the fair driver; a changed
 * count is a decision, not drift.
 *
 * <p>Recorded, not pinned (Oct 2026, the shape pinned below): the ordering
 * pass takes 851,962 steps as written and 56,497 swapped — it reorders
 * nothing here (both conjuncts price ∞) and pays its two-pass walk at every
 * unfolding of the order it inherits. The parked lane's constant factor over
 * the best hand order is the same walk, paid once.
 */
public class ParkingBenchmarkTest {

	private static <T> long steps(Query query, Unifiable<T> out) {
		AtomicLong count = new AtomicLong();
		StepListener counting = new StepListener() {
			@Override
			public void onStep(Fiber<?> node, Scope scope, String name) {
				count.incrementAndGet();
			}
		};
		query.on(fiber -> new BreadthFirstScheduler<>(fiber).withListener(counting)).solve(out)
				.collect(Collectors.toList());
		return count.get();
	}

	private static <T> long depthFirstStepsToFirst(Query query, Unifiable<T> out) {
		AtomicLong count = new AtomicLong();
		StepListener counting = new StepListener() {
			@Override
			public void onStep(Fiber<?> node, Scope scope, String name) {
				count.incrementAndGet();
			}
		};
		query.on(fiber -> DepthFirstScheduler.of(fiber).withListener(counting)).solve(out)
				.limit(1)
				.forEach(r -> {
				});
		return count.get();
	}

	private static Query parked(Goal g) {
		return Query.of(g).optimized(new ParkingOptimizer());
	}

	/**
	 * R2 scaled: {@code m} disjunctions of {@code k} alternatives, each binding
	 * {@code x} and running {@code appendo} over a list of {@code n}; {@code q}
	 * binds {@code x} behind {@code d} unfoldings. Written as {@code p ∧ q} or
	 * swapped.
	 */
	static Goal determinacyAfterUnfolding(Unifiable<Long> x, int m, int k, int n, int d, boolean qFirst) {
		Goal ps = Goal.success();
		for (int j = 0; j < m; j++) {
			List<Goal> alternatives = new ArrayList<>();
			for (long i = 1; i <= k; i++) {
				Unifiable<LList<Long>> front = lvar(), back = lvar();
				alternatives.add(x.unifies(lval(i)).and(Logic.appendo(front, back, LList.ofAll(n, idx -> lval((long) idx)))));
			}
			ps = ps.and(Goal.defer(() -> Conde.of(alternatives)));
		}
		Goal q = x.unifies(lval(1L));
		for (int i = 0; i < d; i++) {
			Goal inner = q;
			q = Goal.defer(() -> inner);
		}
		return qFirst ? q.and(ps) : ps.and(q);
	}

	@Test
	public void theRaceAtFiveParkedStaysWithinTheExitCycleCost() {
		// SchedulingBenchmarkTest pins eager at 36,900–37,200; parked forks the
		// same 120 schedules at the answer exit instead of at each pair
		List<SchedulingBenchmarkTest.Strip> c = SchedulingBenchmarkTest.sameSpace(5);
		long parked = steps(
				parked(SchedulingBenchmarkTest.schedule(c, 5, SchedulingBenchmarkTest::nonOverlapConde)),
				SchedulingBenchmarkTest.starts(c));
		assertThat(parked).isBetween(37_300L, 37_700L);

		// answer-set parity under the chaos harness
		List<SchedulingBenchmarkTest.Strip> a = SchedulingBenchmarkTest.sameSpace(5);
		List<SchedulingBenchmarkTest.Strip> b = SchedulingBenchmarkTest.sameSpace(5);
		assertThat(new HashSet<>(answers(parked(SchedulingBenchmarkTest.schedule(a, 5, SchedulingBenchmarkTest::nonOverlapConde)), SchedulingBenchmarkTest.starts(a))))
				.isEqualTo(new HashSet<>(answers(Query.of(SchedulingBenchmarkTest.schedule(b, 5, SchedulingBenchmarkTest::nonOverlapConde)), SchedulingBenchmarkTest.starts(b))));
	}

	@Test
	public void theGenesisProblemParkedStaysWithinTheExitCycleCost() {
		// SchedulingBenchmarkTest pins eager at 1,600–2,000 depth-first to the first answer
		List<SchedulingBenchmarkTest.Strip> ten = SchedulingBenchmarkTest.sameSpace(10);
		long parked = depthFirstStepsToFirst(
				parked(SchedulingBenchmarkTest.schedule(ten, 500, SchedulingBenchmarkTest::nonOverlapConde)),
				SchedulingBenchmarkTest.starts(ten));
		assertThat(parked).isBetween(1_700L, 2_000L);
	}

	@Test
	public void conjunctOrderIsIrrelevantUnderParking() {
		// two 16-way disjunctions before a conjunct determinate four unfoldings deep:
		// eager pays the product of the dead forks as written (71,608) and only the
		// live one swapped (4,995); parked pays the same either way
		Unifiable<Long> x = lvar();
		long asWritten = steps(parked(determinacyAfterUnfolding(x, 2, 16, 10, 4, false)), x);
		Unifiable<Long> y = lvar();
		long swapped = steps(parked(determinacyAfterUnfolding(y, 2, 16, 10, 4, true)), y);

		assertThat(asWritten).isEqualTo(14_847);
		assertThat(swapped).isEqualTo(14_849);
		assertThat(swapped - asWritten).as("the swapped conjunction's one extra defer").isEqualTo(2);
	}

	@Test
	public void parkedBeatsTheWrittenOrderAndStaysAConstantFactorFromTheBest() {
		Unifiable<Long> x = lvar();
		long eagerAsWritten = steps(Query.of(determinacyAfterUnfolding(x, 2, 16, 10, 4, false)), x);
		Unifiable<Long> y = lvar();
		long eagerBest = steps(Query.of(determinacyAfterUnfolding(y, 2, 16, 10, 4, true)), y);
		Unifiable<Long> z = lvar();
		long parked = steps(parked(determinacyAfterUnfolding(z, 2, 16, 10, 4, false)), z);

		assertThat(eagerAsWritten).isEqualTo(71_608);
		assertThat(eagerBest).isEqualTo(4_995);
		assertThat(parked).isLessThan(eagerAsWritten);
		// the constant is the optimizer walk at each unfolding, paid once
		assertThat(parked).isLessThan(eagerBest * 7 / 2);
	}

	private static List<Reified<LList<Long>>> answers(Query query, Unifiable<LList<Long>> out) {
		return query.on(TestSchedulers.factory()).solve(out).collect(Collectors.toList());
	}
}
