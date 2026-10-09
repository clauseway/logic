package org.clauseway.logic.goals.optimizer;

// ABOUTME: Pins parking: a Conde reached in a conjunction forks at the next barrier,
// ABOUTME: not at its textual position — same answers as eager Conde, fewer runs between.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Logic;
import org.clauseway.logic.unification.structures.LList;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.Test;

public class ParkingOptimizerTest {

	private static <T> List<T> answers(Goal g, Unifiable<T> out) {
		return Query.of(g).optimized(new ParkingOptimizer()).on(TestSchedulers.factory()).solve(out)
				.map(Reified::get)
				.sorted()
				.collect(Collectors.toList());
	}

	private static <T> List<T> eager(Goal g, Unifiable<T> out) {
		return Query.of(g).on(TestSchedulers.factory()).solve(out)
				.map(Reified::get)
				.sorted()
				.collect(Collectors.toList());
	}

	@Test
	public void aParkedDisjunctionForksAtTheAnswerWithTheSameAnswersAsEagerConde() {
		Unifiable<Integer> x = lvar();
		Goal g = unify(x, lval(1)).or(unify(x, lval(2))).or(unify(x, lval(3)));

		assertThat(answers(g, x)).containsExactly(1, 2, 3).isEqualTo(eager(g, x));
	}

	@Test
	public void aPostingAfterTheDisjunctionIsSeenByEveryFork() {
		// textual order says fork, then bind; parked, the bind is in the knowledge every fork sees
		Unifiable<Integer> x = lvar();
		Goal g = unify(x, lval(1)).or(unify(x, lval(2))).or(unify(x, lval(3))).and(unify(x, lval(2)));

		assertThat(answers(g, x)).containsExactly(2).isEqualTo(eager(g, x));
	}

	@Test
	public void determinateWorkAfterTheDisjunctionRunsOnceNotPerFork() {
		Unifiable<Integer> x = lvar();
		AtomicInteger runs = new AtomicInteger();
		Goal counted = s -> {
			runs.incrementAndGet();
			return Cont.just(s);
		};
		Goal g = unify(x, lval(1)).or(unify(x, lval(2))).or(unify(x, lval(3))).and(counted);

		assertThat(eager(g, x)).containsExactly(1, 2, 3);
		int perFork = runs.getAndSet(0);
		assertThat(answers(g, x)).containsExactly(1, 2, 3);

		assertThat(perFork).isEqualTo(3);
		assertThat(runs.get()).isEqualTo(1);
	}

	@Test
	public void nestedDisjunctionsParkWhenTheirAlternativeRuns() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Goal inner = unify(x, lval(1)).or(unify(x, lval(2)));
		Goal g = inner.and(unify(y, lval(5))).or(unify(x, lval(3)).and(unify(y, lval(6))));

		assertThat(answers(g, x)).containsExactly(1, 2, 3).isEqualTo(eager(g, x));
	}

	@Test
	public void twoParkedDisjunctionsForkIntoTheirProduct() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Goal g = unify(x, lval(1)).or(unify(x, lval(2))).and(unify(y, lval(10)).or(unify(y, lval(20))));
		List<Integer> sums = Query.of(g).optimized(new ParkingOptimizer()).on(TestSchedulers.factory()).stream()
				.map(k -> k.substitution().walk(x).get() + k.substitution().walk(y).get())
				.sorted()
				.collect(Collectors.toList());

		assertThat(sums).containsExactly(11, 12, 21, 22);
	}

	@Test
	public void committedChoiceJudgesEachForkedBranch() {
		Unifiable<Integer> x = lvar();
		Goal g = unify(x, lval(1)).or(unify(x, lval(2))).or(unify(x, lval(3)))
				.and(Goal.condu(unify(x, lval(2)), Goal.success()));

		assertThat(answers(g, x)).containsExactly(1, 2, 3);
	}

	@Test
	public void aRecursiveRelationParksThroughTheDeferHook() {
		Unifiable<LList<Integer>> x = lvar(), y = lvar();
		Unifiable<Integer> a = lvar();
		Goal splits = Logic.appendo(x, y, LList.ofAll(1, 2, 3));
		Goal pinned = Logic.appendo(x, y, LList.ofAll(1, 2, 3)).and(unify(x, LList.of(a)));

		assertThat(Query.of(splits).optimized(new ParkingOptimizer()).on(TestSchedulers.factory()).solve(y).count()).isEqualTo(4);
		assertThat(answers(pinned, a)).containsExactly(1);
	}
}
