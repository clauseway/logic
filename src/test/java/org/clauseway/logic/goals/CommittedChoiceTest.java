package org.clauseway.logic.goals;

// ABOUTME: Pins miniKanren's committed-choice pair: conda keeps every answer of the
// ABOUTME: clause it commits to (soft cut), condu keeps one (committed choice).

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class CommittedChoiceTest {

	private static List<Integer> answers(Goal g, Unifiable<Integer> x) {
		return Query.of(g).on(TestSchedulers.factory()).solve(x)
				.map(Reified::get)
				.sorted()
				.collect(Collectors.toList());
	}

	@Test
	public void condaKeepsEveryAnswerOfTheClauseItCommitsTo() {
		Unifiable<Integer> x = lvar();
		Goal g = Goal.conda(unify(x, lval(1)).or(unify(x, lval(2))), unify(x, lval(3)));

		assertThat(answers(g, x)).containsExactly(1, 2);
	}

	@Test
	public void conduKeepsOneAnswerOfTheClauseItCommitsTo() {
		Unifiable<Integer> x = lvar();
		Goal g = Goal.condu(unify(x, lval(1)).or(unify(x, lval(2))), unify(x, lval(3)));

		assertThat(answers(g, x)).hasSize(1).isSubsetOf(1, 2);
	}

	@Test
	public void bothPassOverAClauseWithoutAnswers() {
		Unifiable<Integer> x = lvar();

		assertThat(answers(Goal.conda(Goal.failure(), unify(x, lval(3))), x)).containsExactly(3);
		assertThat(answers(Goal.condu(Goal.failure(), unify(x, lval(3))), x)).containsExactly(3);
	}
}
