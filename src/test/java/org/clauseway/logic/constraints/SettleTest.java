package org.clauseway.logic.constraints;

// ABOUTME: Pins settling: every Pending store runs the search it owes, to
// ABOUTME: quiescence, before an answer leaves or a committed choice judges.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import org.junit.Test;

public class SettleTest {

	/** Owes one fork of {@code x} over its values, then nothing. */
	@Value
	private static class Choice implements Pending {
		Unifiable<Integer> x;
		List<Integer> values;

		@Override
		public boolean pending() {
			return !values.isEmpty();
		}

		@Override
		public Goal settle() {
			Choice settled = new Choice(x, Arrays.asList());
			return Conde.of(values.stream()
					.map(v -> unify(x, lval(v)).and(s -> Cont.just(s.putStore(settled))))
					.collect(Collectors.toList()));
		}
	}

	/** A second store class, so two citizens can owe work in one package. */
	@Value
	private static class OtherChoice implements Pending {
		Unifiable<Integer> y;
		List<Integer> values;

		@Override
		public boolean pending() {
			return !values.isEmpty();
		}

		@Override
		public Goal settle() {
			OtherChoice settled = new OtherChoice(y, Arrays.asList());
			return Conde.of(values.stream()
					.map(v -> unify(y, lval(v)).and(s -> Cont.just(s.putStore(settled))))
					.collect(Collectors.toList()));
		}
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
		Knowledge root = Knowledge.empty().withStore(new Choice(x, Arrays.asList(1, 2, 3)));

		assertThat(values(Query.of(Goal.success()).from(root), x)).containsExactly(1, 2, 3);
	}

	@Test
	public void everyCitizenSettlesAndTheChildrenReEnterThePhase() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Knowledge root = Knowledge.empty()
				.withStore(new Choice(x, Arrays.asList(1, 2)))
				.withStore(new OtherChoice(y, Arrays.asList(10, 20)));
		List<Integer> sums = Query.of(Goal.success()).from(root).on(TestSchedulers.factory()).stream()
				.map(k -> k.substitution().walk(x).get() + k.substitution().walk(y).get())
				.sorted()
				.collect(Collectors.toList());

		assertThat(sums).containsExactly(11, 12, 21, 22);
	}

	@Test
	public void theRawRunExpandsToo() {
		Unifiable<Integer> x = lvar();
		Knowledge root = Knowledge.empty().withStore(new Choice(x, Arrays.asList(1, 2, 3)));

		assertThat(Query.of(Goal.success()).from(root).on(TestSchedulers.factory()).stream().count()).isEqualTo(3);
	}

	@Test
	public void committedChoiceJudgesEachExpandedBranch() {
		// without the phase at the judge, condu commits to x ≡ 2 while x is still
		// open and the later fork keeps only 2; with it, each value is judged
		Unifiable<Integer> x = lvar();
		Knowledge root = Knowledge.empty().withStore(new Choice(x, Arrays.asList(1, 2, 3)));
		Goal judged = Goal.condu(unify(x, lval(2)), Goal.success());

		assertThat(values(Query.of(judged).from(root), x)).containsExactly(1, 2, 3);
	}
}
