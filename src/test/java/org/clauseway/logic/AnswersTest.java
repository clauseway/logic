package org.clauseway.logic;

// ABOUTME: Pins the ask stage: bag semantics per derivation, the token doors
// ABOUTME: (anchor default, explicit routing), residues arriving as guarded
// ABOUTME: conditions, the bare-values refusal, and the suspensions refusal.

import static org.clauseway.logic.finitedomain.FiniteDomain.dom;
import static org.clauseway.logic.nogoods.Exclusion.exclude;
import static org.clauseway.logic.projection.Projection.project;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.clauseway.logic.finitedomain.Longs;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.solving.Answer;
import org.clauseway.logic.solving.Condition;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class AnswersTest {

	@Test
	public void askEmitsOneAnswerPerDerivationDuplicatesIncluded() {
		Unifiable<Integer> x = lvar();
		List<Answer<Unifiable<Integer>>> answers = Utils.collect(
				Query.of(x.unifies(1).or(x.unifies(1))).ask(x).each());
		assertThat(answers).hasSize(2);
		assertThat(answers.get(0)).isEqualTo(answers.get(1));
	}

	@Test
	public void theAnswerCarriesTheAnchorImageUnconditionally() {
		Unifiable<Integer> x = lvar();
		List<Answer<Unifiable<Integer>>> answers = Utils.collect(
				Query.of(x.unifies(7)).ask(x).each());
		assertThat(answers).hasSize(1);
		assertThat(answers.get(0).getReified().get()).isEqualTo(7);
		assertThat(answers.get(0).getCondition()).isSameAs(Condition.ONE);
	}

	@Test
	public void theDefaultTokenIsTheAnchor() {
		Unifiable<Integer> x = lvar();
		List<Answer<Unifiable<Integer>>> answers = Utils.collect(
				Query.of(x.unifies(1)).ask(x).each());
		assertThat(answers.get(0).getRelation()).isSameAs(x);
	}

	@Test
	public void anExplicitTokenRoutes() {
		Unifiable<Integer> x = lvar();
		List<Answer<String>> answers = Utils.collect(
				Query.of(x.unifies(1)).ask("users", x).each());
		assertThat(answers.get(0).getRelation()).isEqualTo("users");
	}

	@Test
	public void enforcementLabelsDomainsExactlyLikeSolve() {
		// ask runs the same enforce stage reify does: a bare domain LABELS,
		// forking one ground unconditional answer per value — never one
		// wide answer with the domain smuggled into the condition
		Unifiable<Long> x = lvar();
		List<Answer<Unifiable<Long>>> answers = Utils.collect(
				Query.of(dom(x, Longs.range(1, 4))).ask(x).each());
		assertThat(answers).hasSize(3);
		assertThat(answers.stream()
				.map(a -> (Long) a.getReified().get())
				.collect(Collectors.toList()))
				.containsExactlyInAnyOrder(1L, 2L, 3L);
		assertThat(answers.stream().allMatch(a -> a.getCondition().isOne())).isTrue();
	}

	@Test
	public void aResidueSurvivingEnforcementArrivesAsAGuardedCondition() {
		// a live nogood survives enforce (the record rides the answer) —
		// the same residue a regular solve renders through Constrained
		Unifiable<Integer> x = lvar();
		List<Answer<Unifiable<Integer>>> answers = Utils.collect(
				Query.of(exclude(x.unifies(3))).ask(x).each());
		assertThat(answers).hasSize(1);
		assertThat(answers.get(0).getCondition().isOne()).isFalse();
	}

	@Test
	public void valuesHandsBareValuesForUnconditionalAnswers() {
		Unifiable<Integer> x = lvar();
		try (Stream<Reified<Integer>> values = Query.of(x.unifies(7)).ask(x).values()) {
			assertThat(values.map(Term::get).collect(Collectors.toList()))
					.containsExactly(7);
		}
	}

	@Test
	public void valuesRefusesAGuardedAnswer() {
		Unifiable<Integer> x = lvar();
		assertThatThrownBy(() -> {
			try (Stream<Reified<Integer>> values = Query.of(exclude(x.unifies(3))).ask(x).values()) {
				values.count();
			}
		})
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("guarded");
	}

	@Test
	public void askRefusesPendingSuspensions() {
		Unifiable<Integer> pending = lvar();
		Unifiable<Integer> x = lvar();
		Goal goal = project(pending, v -> Goal.success()).and(x.unifies(1));
		assertThatThrownBy(() -> Utils.collect(Query.of(goal).ask(x).each()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("suspensions");
	}
}
