package org.clauseway.logic.goals;

// ABOUTME: Pins the inner solve: settled on entry and on every exit, read only after
// ABOUTME: its seal, and closed under a watermark when asked.

import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.constraints.Propagation;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class SubsolveTest {

	/** A parked disjunction of {@code x} over its values: forced at the next barrier. */
	private static Goal parked(Unifiable<Integer> x, Integer... values) {
		return Propagation.park(Conde.of(Arrays.stream(values)
				.map(v -> x.unifies(lval(v)))
				.collect(Collectors.toList())));
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
		Knowledge[] from = new Knowledge[1];
		parked(x, 1, 2, 3).apply(Knowledge.empty()).apply(k -> {
			from[0] = k;
			return Fiber.done(Nothing.nothing());
		}).ground();

		List<Knowledge> seen = Subsolve.of(Goal.success()).collect(from[0]).ground();

		assertThat(walked(seen, x)).containsExactly(1, 2, 3);
	}

	@Test
	public void everyExitIsSettledBeforeTheConsumerSeesIt() {
		Unifiable<Integer> x = lvar();
		List<Knowledge> seen = new ArrayList<>();

		Fiber<Nothing> done = Subsolve.of(parked(x, 1, 2)).each(Knowledge.empty(), k -> {
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
