package org.clauseway.logic;

// ABOUTME: Pins the select pipeline: Selection's extraction and multiplicity
// ABOUTME: switches (enforced+distinct default, raw/all explicit), Stream<Row>
// ABOUTME: delivery, and Row's Reified-typed keyed get.

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
import org.clauseway.logic.solving.Condition;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class SelectionTest {

	private static List<Row> rows(Stream<Row> stream) {
		try (Stream<Row> rows = stream) {
			return rows.collect(Collectors.toList());
		}
	}

	@Test
	public void aRowPerSolutionWithTypedCells() {
		Unifiable<Integer> x = lvar();
		List<Row> rows = rows(Query.of(x.unifies(1).or(x.unifies(2))).select(x).rows());
		assertThat(rows).hasSize(2);
		assertThat(rows.stream()
				.map(row -> row.get(x).get())
				.collect(Collectors.toList()))
				.containsExactlyInAnyOrder(1, 2);
	}

	@Test
	public void oneRowCarriesOneDerivationWholesale() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		List<Row> rows = rows(Query.of(
				x.unifies(1).and(y.unifies(2))
						.or(x.unifies(3).and(y.unifies(4))))
				.select(x, y).rows());
		assertThat(rows).hasSize(2);
		for (Row row : rows) {
			int cellX = row.get(x).get();
			int cellY = row.get(y).get();
			assertThat(cellY - cellX).isEqualTo(1);
		}
	}

	@Test
	public void getRefusesAVariableTheSelectDidNotName() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> stranger = lvar();
		List<Row> rows = rows(Query.of(x.unifies(1)).select(x).rows());
		assertThatThrownBy(() -> rows.get(0).get(stranger))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void distinctIsTheDefault() {
		Unifiable<Integer> x = lvar();
		List<Row> rows = rows(Query.of(x.unifies(1).or(x.unifies(1))).select(x).rows());
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getCondition()).isSameAs(Condition.ONE);
	}

	@Test
	public void allKeepsTheDerivations() {
		Unifiable<Integer> x = lvar();
		List<Row> rows = rows(Query.of(x.unifies(1).or(x.unifies(1))).select(x).all().rows());
		assertThat(rows).hasSize(2);
	}

	@Test
	public void enforcedIsTheDefaultAndLabelsLikeSolve() {
		Unifiable<Long> x = lvar();
		List<Row> rows = rows(Query.of(dom(x, Longs.range(1, 4))).select(x).rows());
		assertThat(rows).hasSize(3);
		assertThat(rows.stream()
				.map(row -> row.get(x).get())
				.collect(Collectors.toList()))
				.containsExactlyInAnyOrder(1L, 2L, 3L);
		assertThat(rows.stream().allMatch(row -> row.getCondition().isOne())).isTrue();
	}

	@Test
	public void rawKeepsTheRegionTheDerivationDenotes() {
		Unifiable<Long> x = lvar();
		List<Row> rows = rows(Query.of(dom(x, Longs.range(1, 4))).select(x).raw().rows());
		assertThat(rows).hasSize(1);
		Reified<Long> cell = rows.get(0).get(x);
		assertThat(cell.isVal()).isFalse();
		assertThat(rows.get(0).getCondition().isOne()).isFalse();
	}

	@Test
	public void aResidueSurvivingEnforcementGuardsTheRow() {
		Unifiable<Integer> x = lvar();
		List<Row> rows = rows(Query.of(exclude(x.unifies(3))).select(x).rows());
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get(x).isVal()).isFalse();
		assertThat(rows.get(0).getCondition().isOne()).isFalse();
	}

	@Test
	public void guardedDerivationsOfOneImageFoldTheirConditions() {
		Unifiable<Integer> x = lvar();
		List<Row> rows = rows(Query.of(exclude(x.unifies(3)).or(exclude(x.unifies(4))))
				.select(x).rows());
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getCondition().conjuncts().size()).isEqualTo(2);
	}

	@Test
	public void selectRefusesPendingSuspensions() {
		Unifiable<Integer> pending = lvar();
		Unifiable<Integer> x = lvar();
		Goal goal = project(pending, v -> Goal.success()).and(x.unifies(1));
		assertThatThrownBy(() -> rows(Query.of(goal).select(x).rows()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("suspensions");
	}

	@Test
	public void anEmptyQuestionStreamsNothing() {
		Unifiable<Integer> x = lvar();
		assertThat(rows(Query.of(x.unifies(1).and(x.unifies(2))).select(x).rows())).isEmpty();
	}
}
