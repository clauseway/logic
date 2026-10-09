package org.clauseway.logic.constraints;

// ABOUTME: Pins settling: every parked disjunction is forced, to quiescence,
// ABOUTME: before an answer leaves or a committed choice judges.

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
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class SettleTest {

	/** A parked disjunction of {@code x} over its values: forced at the next barrier. */
	private static Goal parked(Unifiable<Integer> x, Integer... values) {
		return Propagation.park(Conde.of(Arrays.stream(values)
				.map(v -> unify(x, lval(v)))
				.collect(Collectors.toList())));
	}

	private static <T> List<T> values(Query query, Unifiable<T> out) {
		return query.on(TestSchedulers.factory()).solve(out)
				.map(Reified::get)
				.sorted()
				.collect(Collectors.toList());
	}

	@Test
	public void anOwedForkExpandsBeforeTheAnswerLeaves() {
		Unifiable<Integer> x = lvar();
		assertThat(values(Query.of(parked(x, 1, 2, 3)), x)).containsExactly(1, 2, 3);
	}

	@Test
	public void everyParkedDisjunctionSettlesAndTheChildrenReEnterThePhase() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		List<Integer> sums = Query.of(parked(x, 1, 2).and(parked(y, 10, 20))).on(TestSchedulers.factory()).stream()
				.map(k -> k.substitution().walk(x).get() + k.substitution().walk(y).get())
				.sorted()
				.collect(Collectors.toList());

		assertThat(sums).containsExactly(11, 12, 21, 22);
	}

	@Test
	public void theRawRunExpandsToo() {
		Unifiable<Integer> x = lvar();
		assertThat(Query.of(parked(x, 1, 2, 3)).on(TestSchedulers.factory()).stream().count()).isEqualTo(3);
	}

	@Test
	public void committedChoiceJudgesEachExpandedBranch() {
		// without the phase at the judge, condu commits to x ≡ 2 while x is still
		// open and the later fork keeps only 2; with it, each value is judged
		Unifiable<Integer> x = lvar();
		Goal judged = parked(x, 1, 2, 3).and(Goal.condu(unify(x, lval(2)), Goal.success()));

		assertThat(values(Query.of(judged), x)).containsExactly(1, 2, 3);
	}
}
