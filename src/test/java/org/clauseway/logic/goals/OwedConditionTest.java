package org.clauseway.logic.goals;

// ABOUTME: Pins what a closed read does with an owed condition: the enclosing
// ABOUTME: branch's are set aside on entry, its own refuse at exit.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.aggregate.Aggregate;
import org.clauseway.logic.constraints.Subsolve;
import org.clauseway.logic.projection.Projection;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class OwedConditionTest {

	private static <T> List<T> answers(Goal g, Unifiable<T> out) {
		return Query.of(g).on(TestSchedulers.factory()).solve(out)
				.map(Reified::get)
				.collect(Collectors.toList());
	}

	@Test
	public void anOuterOwedConditionRidesThroughAnAggregate() {
		// project(z) parks unripe; the aggregate's answers do not owe it — z is bound afterwards
		Unifiable<Integer> z = lvar(), n = lvar();
		Goal g = Projection.project(z, v -> Goal.success())
				.and(Aggregate.count((Unifiable<Integer> x) -> x.unifies(lval(1)), n))
				.and(z.unifies(lval(7)));

		assertThat(answers(g, n)).containsExactly(1);
	}

	@Test
	public void anAggregatesOwnOwedConditionRefusesAtItsExit() {
		Unifiable<Integer> n = lvar();
		Goal g = Aggregate.count((Unifiable<Integer> x) ->
				Logic.<Integer> exist(y -> Projection.project(y, v -> Goal.success()).and(x.unifies(lval(1)))), n);

		assertThatThrownBy(() -> answers(g, n))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("owed condition");
	}

	@Test
	public void aClosedReadRefusesItsOwnOwedConditionAndSetsTheEnclosingOnesAside() {
		Unifiable<Integer> z = lvar();
		Knowledge[] under = new Knowledge[1];
		Projection.project(z, v -> Goal.success()).apply(Knowledge.empty()).apply(k -> {
			under[0] = k;
			return Fiber.done(Nothing.nothing());
		}).ground();

		assertThat(Subsolve.closed(Goal.success(), Watermark.now()).collect(under[0]).ground()).hasSize(1);

		// the read's own: a variable born inside the mark, projected and never bound
		Watermark mark = Watermark.now();
		Unifiable<Integer> y = lvar();
		Goal owes = Projection.project(y, v -> Goal.success());
		assertThatThrownBy(() -> Subsolve.closed(owes, mark).collect(Knowledge.empty()).ground())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("owed condition");
	}
}
