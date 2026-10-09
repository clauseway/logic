package org.clauseway.logic.goals;

// ABOUTME: Pins the inner solve: settled on entry and on every exit, read only after
// ABOUTME: its seal, and closed under a watermark when asked.

import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.constraints.Pending;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import org.junit.Test;

public class SubsolveTest {

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
			Choice settled = new Choice(x, new ArrayList<>());
			return Conde.of(values.stream()
					.map(v -> x.unifies(lval(v)).and(s -> Cont.just(s.putStore(settled))))
					.collect(Collectors.toList()));
		}
	}

	private static List<Integer> walked(List<Knowledge> answers, Unifiable<Integer> x) {
		return answers.stream()
				.map(k -> k.substitution().walk(x).get())
				.sorted()
				.collect(Collectors.toList());
	}

	@Test
	public void theEntryIsSettledBeforeTheGoalRuns() {
		Unifiable<Integer> x = lvar();
		Knowledge from = Knowledge.empty().withStore(new Choice(x, Arrays.asList(1, 2, 3)));

		List<Knowledge> seen = Subsolve.of(Goal.success()).collect(from).ground();

		assertThat(walked(seen, x)).containsExactly(1, 2, 3);
	}

	@Test
	public void everyExitIsSettledBeforeTheConsumerSeesIt() {
		Unifiable<Integer> x = lvar();
		Goal parks = s -> Cont.just(s.withStore(new Choice(x, Arrays.asList(1, 2))));
		List<Knowledge> seen = new ArrayList<>();

		Fiber<Nothing> done = Subsolve.of(parks).each(Knowledge.empty(), k -> {
			seen.add(k);
			return Fiber.done(Nothing.nothing());
		});
		done.ground();

		assertThat(walked(seen, x)).containsExactly(1, 2);
	}

	@Test
	public void theReadWaitsForTheSeal() {
		Unifiable<Integer> x = lvar();
		Goal three = x.unifies(lval(1)).or(x.unifies(lval(2))).or(x.unifies(lval(3)));

		assertThat(walked(Subsolve.of(three).collect(Knowledge.empty()).ground(), x))
				.containsExactly(1, 2, 3);
	}

	@Test
	public void aClosedSubsolveRefusesAnOutsideVariable() {
		Unifiable<Integer> outside = lvar();
		Watermark mark = Watermark.now();

		assertThatThrownBy(() -> Subsolve.of(outside.unifies(lval(1))).closed(mark)
				.collect(Knowledge.empty()).ground())
				.isInstanceOf(IllegalStateException.class);
	}
}
