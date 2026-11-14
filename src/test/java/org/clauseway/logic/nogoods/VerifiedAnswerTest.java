package org.clauseway.logic.nogoods;

// ABOUTME: Pins that an answer leaves a solve judged against everything the solve learned,
// ABOUTME: narrowings included: a refuted nogood is settled, an entailed claim fails the row.

import static org.assertj.core.api.Assertions.assertThat;
import static org.clauseway.logic.finitedomain.FiniteDomain.dom;
import static org.clauseway.logic.nogoods.Exclusion.exclude;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;
import org.clauseway.functional.fibers.interpreter.ScopeProfiler;
import org.clauseway.logic.finitedomain.Ints;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.solving.Query;
import org.clauseway.logic.solving.Row;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

/**
 * A nogood's literals are another family's knowledge asleep in the nogood
 * store, and a trial reads the live package. Knowledge that arrives at a
 * door without binding anything changes what the trial would answer, so
 * the verifiers fold once more before an answer leaves the solve, against
 * the settled package. The raw reading shows the residue as it stands,
 * before labelling would bind the variable and re-verify anyway.
 */
public class VerifiedAnswerTest {

	private static String rawRow(Goal goal, Unifiable<Integer> x) {
		List<Row> rows = Query.of(goal).select(x).raw().stream().collect(Collectors.toList());
		assertThat(rows).hasSize(1);
		return rows.get(0).toString();
	}

	@Test
	public void aNarrowingAtADoorSettlesTheNogoodItRefutes() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		String row = rawRow(dom(x, Ints.interval(0, 10))
				.and(exclude(Ints.lss(x, lval(3))))
				.and(y.unifies(5))
				.and(Ints.lss(y, x)), x);
		assertThat(row).doesNotContain("¬(");
	}

	@Test
	public void theResidueDoesNotDependOnWhichSideOfTheNogoodArrivesFirst() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		String nogoodFirst = rawRow(dom(x, Ints.interval(0, 10))
				.and(exclude(Ints.lss(x, lval(3))))
				.and(y.unifies(5))
				.and(Ints.lss(y, x)), x);
		String narrowingFirst = rawRow(dom(x, Ints.interval(0, 10))
				.and(y.unifies(5))
				.and(Ints.lss(y, x))
				.and(exclude(Ints.lss(x, lval(3)))), x);
		assertThat(nogoodFirst).isEqualTo(narrowingFirst);
	}

	@Test
	public void anArrivalThatNarrowsNothingStillSettlesTheNogoodItRefutes() {
		// lss(5, x) over an undomained x parks without narrowing; the nogood's trial
		// still reads it, since imposing x ⊂ {0,1} wakes it against 5 < x
		Unifiable<Integer> x = lvar("x");
		String nogoodFirst = rawRow(exclude(dom(x, Ints.enumerated(0, 1)))
				.and(Ints.lss(lval(5), x)), x);
		String arrivalFirst = rawRow(Ints.lss(lval(5), x)
				.and(exclude(dom(x, Ints.enumerated(0, 1)))), x);
		assertThat(arrivalFirst).doesNotContain("¬(");
		assertThat(nogoodFirst).isEqualTo(arrivalFirst);
	}

	/**
	 * A narrowing that entails the denied claim fails the row once the value is read. The raw
	 * reading keeps the nogood owed: a re-imposed propagator is not "nothing changed", and the
	 * cross-off reads conservatively toward owed; labelling binds and the ground trial fails.
	 */
	@Test
	public void aNarrowingThatEntailsTheDeniedClaimFailsTheRow() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Goal goal = dom(x, Ints.interval(0, 10))
				.and(exclude(Ints.lss(x, lval(3))))
				.and(y.unifies(3))
				.and(Ints.lss(x, y));
		assertThat(rawRow(goal, x)).contains("¬(lss(_.0, {3}))");
		assertThat(Query.of(goal).select(x).stream().collect(Collectors.toList())).isEmpty();
	}

	/**
	 * The verifiers fold once, when the answer is read, not on every arrival at a door: two
	 * arrivals after the posting add no door-time steps to the nogood store, and the answer's
	 * reading folds it.
	 */
	@Test
	public void theVerifiersFoldWhenTheAnswerIsReadNotAtEveryDoor() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Goal posted = dom(x, Ints.interval(0, 10)).and(exclude(Ints.lss(x, lval(3))));
		ScopeProfiler postingAlone = new ScopeProfiler();
		Query.of(posted).profiled(postingAlone).select(x).raw().stream().collect(Collectors.toList());
		ScopeProfiler withArrivals = new ScopeProfiler();
		Query.of(posted.and(y.unifies(5)).and(Ints.lss(y, x)))
				.profiled(withArrivals).select(x).raw().stream().collect(Collectors.toList());
		assertThat(stepsOf(withArrivals, "Met @ NogoodConstraints"))
				.isEqualTo(stepsOf(postingAlone, "Met @ NogoodConstraints"));
		assertThat(stepsOf(withArrivals, "Verify @ NogoodConstraints")).isPositive();
	}

	private static long stepsOf(ScopeProfiler profiler, String bucket) {
		return profiler.counts().entrySet().stream()
				.filter(entry -> entry.getKey().contains(bucket))
				.mapToLong(Map.Entry::getValue)
				.sum();
	}
}
