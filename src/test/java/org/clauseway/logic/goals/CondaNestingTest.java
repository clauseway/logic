package org.clauseway.logic.goals;

// ABOUTME: The soft cut under exhaustion: a nested conda inside a clause
// ABOUTME: must not leak the fallback when the head clause has solutions.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.logic.unification.structures.LList;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class CondaNestingTest {

	@Test
	public void aNestedCondaCommitsWithoutLeakingTheFallback() {
		Unifiable<String> x = lvar();
		Goal g = Goal.conda(
				Goal.conda(unify(x, lval("keep")), unify(x, lval("inner-fallback"))),
				unify(x, lval("outer-fallback")));

		List<String> got = Query.of(g).on(TestSchedulers.factory()).solve(x).map(Reified::toString).collect(Collectors.toList());

		assertThat(got).containsExactly("{keep}");
	}

	@Test
	public void aCondaClauseGuardedByProjectionCommits() {
		// filter's clause shape: the guard runs through Logic.project
		Unifiable<Integer> a = lvar();
		Unifiable<String> x = lvar();
		Goal g = unify(a, lval(2)).and(Goal.conda(
				Goal.defer(() -> Logic.project(a, v -> v != 1 ? Goal.success() : Goal.failure())
						.and(unify(x, lval("keep")))),
				unify(x, lval("skip"))));

		List<String> got = Query.of(g).on(TestSchedulers.factory()).solve(x).map(Reified::toString).collect(Collectors.toList());

		assertThat(got).containsExactly("{keep}");
	}

	@Test
	public void filterCommitsPerElement() {
		// SortingTest.filter, shrunk: keep elements != 1 of [1, 2]
		Unifiable<LList<Integer>> out =
				lvar();
		List<String> got = Query.of(filter(LList.ofAll(1, 2, 1, 3, 1, 4), out,
				a -> Logic.project(a, v -> v != 1 ? Goal.success() : Goal.failure()))).on(TestSchedulers.factory()).solve(out).map(Reified::toString).collect(Collectors.toList());

		assertThat(got).hasSize(1);
	}

	private static <A> Goal filter(
			Unifiable<LList<A>> with,
			Unifiable<LList<A>> without,
			java.util.function.Function<Unifiable<A>, Goal> pred) {
		return org.clauseway.logic.goals.Matche.matche(with,
				org.clauseway.logic.goals.Matche.llist(() -> without.unifies(LList.empty())),
				org.clauseway.logic.goals.Matche.llist((a, d) -> Goal.conda(
						Goal.defer(() -> pred.apply(a)
								.and(org.clauseway.logic.goals.Matche.matche(without,
										org.clauseway.logic.goals.Matche.llist((b, e) -> b.unifiesNc(a)
												.and(Goal.defer(() -> filter(d, e, pred))))))),
						Goal.defer(() -> filter(d, without, pred)))));
	}

	@Test
	public void aCondaInsideARecursionCommitsPerLevel() {
		assertThat(countdown(3).size()).isEqualTo(1);
	}

	private static List<String> countdown(int n) {
		Unifiable<String> out = lvar();
		return Query.of(level(n, out)).on(TestSchedulers.factory()).solve(out).map(Reified::toString).collect(Collectors.toList());
	}

	/** level(n): conda(succeed with "hit-n" and recurse; fallback). */
	private static Goal level(int n, Unifiable<String> out) {
		if (n == 0) {
			return unify(out, lval("bottom"));
		}
		return Goal.conda(
				Goal.defer(() -> level(n - 1, out)),
				unify(out, lval("fallback-" + n)));
	}
}
