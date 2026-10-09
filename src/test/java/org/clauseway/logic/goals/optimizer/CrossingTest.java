package org.clauseway.logic.goals.optimizer;

// ABOUTME: Pins the crossing hook: a barrier asks the ambient optimizer on entry and
// ABOUTME: behind each emission, and the optimizer discharges what it parked there.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.goals.Packaged;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.vavr.collection.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Value;
import org.junit.Test;

public class CrossingTest {

	/** The stub pass's own state: disjunctions it parked, forked when a barrier is crossed. */
	@Value
	static class Parked implements Packaged {
		List<Goal> disjunctions;

		static Parked in(Knowledge p) {
			return p.getStores().get(Parked.class).map(Parked.class::cast).getOrElse(new Parked(List.empty()));
		}
	}

	/** A pass that rewrites nothing and discharges its parked disjunctions at every crossing. */
	static class Stub implements Optimizer {
		@Override
		public Optional<Cont<Knowledge, Nothing>> crossing(Knowledge p) {
			Parked parked = Parked.in(p);
			if (parked.getDisjunctions().isEmpty()) {
				return Optional.empty();
			}
			Goal first = parked.getDisjunctions().head();
			Knowledge rest = p.putStore(new Parked(parked.getDisjunctions().tail()));
			return Optional.of(first.apply(rest).flatMap(child -> crossing(child).orElseGet(() -> Cont.just(child))));
		}
	}

	/** Parks a disjunction of {@code x} over its values in the stub's store. */
	static Goal parked(Unifiable<Integer> x, Integer... values) {
		Goal disjunction = Conde.of(Arrays.stream(values).map(v -> unify(x, lval(v))).collect(Collectors.toList()));
		return s -> Cont.just(s.putStore(new Parked(Parked.in(s).getDisjunctions().append(disjunction))));
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

		assertThat(values(Query.of(parked(x, 1, 2, 3)).optimized(new Stub()), x)).containsExactly(1, 2, 3);
	}

	@Test
	public void everyParkedDisjunctionIsDischargedAndTheChildrenCrossAgain() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		java.util.List<Integer> sums = Query.of(parked(x, 1, 2).and(parked(y, 10, 20)))
				.optimized(new Stub()).on(TestSchedulers.factory()).stream()
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

		assertThat(values(Query.of(judged).optimized(new Stub()), x)).containsExactly(1, 2, 3);
	}

	@Test
	public void withoutAnOptimizerABarrierIsStraightThrough() {
		Unifiable<Integer> x = lvar();

		assertThat(values(Query.of(Barrier.of(unify(x, lval(5)))), x)).containsExactly(5);
	}
}
