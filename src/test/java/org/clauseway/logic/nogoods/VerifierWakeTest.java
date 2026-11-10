package org.clauseway.logic.nogoods;

// ABOUTME: Pins that a verifier re-verifies when another family's knowledge moves at a
// ABOUTME: door, not only at a binding: a narrowing that refutes an owed nogood settles it.

import static org.assertj.core.api.Assertions.assertThat;
import static org.clauseway.logic.finitedomain.FiniteDomain.dom;
import static org.clauseway.logic.nogoods.Exclusion.exclude;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.List;
import java.util.stream.Collectors;
import org.clauseway.logic.finitedomain.Ints;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.solving.Query;
import org.clauseway.logic.solving.Row;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

/**
 * A nogood's literals are another family's knowledge asleep in the nogood
 * store, and a trial reads the live package. A {@code Met} that narrows a
 * domain changes what the trial would answer, so the verifier must fold on
 * it as it does on a {@code Bind}. The raw reading shows the residue as it
 * stands, before labelling would bind the variable and re-verify anyway.
 */
public class VerifierWakeTest {

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
}
