package org.clauseway.logic.projection;

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import org.clauseway.logic.unification.terms.LVal;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.functional.tuples.Tuple;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ProjectionTest {

	@Test
	public void shouldJustProjectImmediately() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();

		List<Integer> results = Query.of(x.unifies(3)
				.and(Projection.project(x, v -> y.unifies(2 * v)))).on(TestSchedulers.factory()).solve(y)
				.map(Term::get)
				.collect(Collectors.toList());

		Assertions.assertThat(results)
				.containsExactly(6);
	}

	@Test
	public void shouldProjectDeferred() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();

		List<Integer> results =
				Query.of(Projection.project(x, v -> y.unifies(2 * v))
						.and(x.unifies(3))).on(TestSchedulers.factory()).solve(y)
						.map(Term::get)
						.collect(Collectors.toList());

		Assertions.assertThat(results)
				.containsExactly(6);
	}

	@Test
	public void shouldProjectDeferredWithOtherVariable() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Unifiable<Integer> z = lvar();

		List<Integer> results =
				Query.of(Projection.project(y, v -> z.unifies(2 * v))
						.and(x.unifies(y))
						.and(y.unifies(3))).on(TestSchedulers.factory()).solve(z)
						.map(Term::get)
						.collect(Collectors.toList());

		Assertions.assertThat(results)
				.containsExactly(6);
	}

	@Test
	public void compositeProjectionWakesOnMemberBindings() {
		// watched is a STRUCTURE; members bind one at a time, including a member
		// that only exists after nested instantiation
		Unifiable<Integer> a = lvar();
		Unifiable<Integer> b = lvar();
		Unifiable<Integer> out = lvar();

		List<Integer> results = Query.of(Projection
				.project(LVal.lval(Tuple.of(a, b)),
						t -> out.unifies(t._1.get() + t._2.get()))
				.and(a.unifies(1))
				.and(b.unifies(2))).on(TestSchedulers.factory()).solve(out)
				.map(Term::get)
				.collect(Collectors.toList());

		Assertions.assertThat(results).containsExactly(3);
	}

	@Test
	public void twoVariableProjectionFiresOnceBothGround() {
		Unifiable<Integer> a = lvar();
		Unifiable<Integer> b = lvar();
		Unifiable<Integer> out = lvar();

		List<Integer> results = Query.of(Projection
				.project(a, b, (x, y) -> out.unifies(x * y))
				.and(a.unifies(3))
				.and(b.unifies(4))).on(TestSchedulers.factory()).solve(out)
				.map(Term::get)
				.collect(Collectors.toList());

		org.assertj.core.api.Assertions.assertThat(results).containsExactly(12);
	}
}
