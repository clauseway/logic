package org.clauseway.logic.goals.optimizer;

// ABOUTME: Pins the barrier hooks: a barrier asks the ambient optimizer on entry and
// ABOUTME: as each answer leaves, and the parking pass discharges what it parked there.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.Test;

public class CrossingTest {

	/** A disjunction of {@code x} over its values, parked rather than forked. */
	static Goal parked(Unifiable<Integer> x, Integer... values) {
		return new Parking(Conde.of(Arrays.stream(values).map(v -> unify(x, lval(v))).collect(Collectors.toList())));
	}

	private static <T> java.util.List<T> values(Query query, Unifiable<T> out) {
		return query.on(TestSchedulers.factory()).solve(out)
				.map(Reified::get)
				.sorted()
				.collect(Collectors.toList());
	}

	@Test
	public void aParkedDisjunctionIsDischargedBeforeTheAnswerLeaves() {
		Unifiable<Integer> x = lvar();

		assertThat(values(Query.of(parked(x, 1, 2, 3)).optimized(new ParkingOptimizer()), x)).containsExactly(1, 2, 3);
	}

	@Test
	public void everyParkedDisjunctionIsDischargedAndTheChildrenCrossAgain() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		java.util.List<Integer> sums = Query.of(parked(x, 1, 2).and(parked(y, 10, 20)))
				.optimized(new ParkingOptimizer()).on(TestSchedulers.factory()).stream()
				.map(k -> k.substitution().walk(x).get() + k.substitution().walk(y).get())
				.sorted()
				.collect(Collectors.toList());

		assertThat(sums).containsExactly(11, 12, 21, 22);
	}

	@Test
	public void committedChoiceJudgesEachDischargedBranch() {
		// the judge is a crossing: the fork happens before any alternative is tried
		Unifiable<Integer> x = lvar();
		Goal judged = parked(x, 1, 2, 3).and(Goal.condu(unify(x, lval(2)), Goal.success()));

		assertThat(values(Query.of(judged).optimized(new ParkingOptimizer()), x)).containsExactly(1, 2, 3);
	}

	@Test
	public void withoutAnOptimizerABarrierIsStraightThrough() {
		Unifiable<Integer> x = lvar();

		assertThat(values(Query.of(Barrier.of(unify(x, lval(5)))), x)).containsExactly(5);
	}
}
