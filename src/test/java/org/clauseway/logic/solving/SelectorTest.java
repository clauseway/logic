package org.clauseway.logic.solving;

// ABOUTME: Pins the mapped reading of a select: a Selector names the variables it
// ABOUTME: needs and maps each row into Java; a cell that is not ground is refused where map reads it.

import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class SelectorTest {

	@Value
	private static class Point {
		int x;
		int y;
	}

	/** A point's two coordinates as the variables a goal binds, mapped into the value. */
	private static class PointSelector implements Selector<Point> {
		final Unifiable<Integer> x = lvar();
		final Unifiable<Integer> y = lvar();
		final AtomicInteger mapped = new AtomicInteger();

		@Override
		public Iterable<? extends Unifiable<?>> keys() {
			return Arrays.asList(x, y);
		}

		@Override
		public Point map(Row row) {
			mapped.incrementAndGet();
			return new Point(row.get(x), row.get(y));
		}
	}

	/** The point selector naming its keys as a row type would. */
	private static class NamedPointSelector extends PointSelector {
		@Override
		public Iterable<String> names() {
			return Arrays.asList("x", "y");
		}
	}

	private static <D> List<D> all(Stream<D> stream) {
		try (Stream<D> rows = stream) {
			return rows.collect(Collectors.toList());
		}
	}

	@Test
	public void aMapperMapsEverySolutionIntoJava() {
		PointSelector point = new PointSelector();
		List<Point> points = all(Query.of(
				point.x.unifies(1).and(point.y.unifies(2))
						.or(point.x.unifies(3).and(point.y.unifies(4))))
				.select(point).stream());
		assertThat(points).containsExactlyInAnyOrder(new Point(1, 2), new Point(3, 4));
	}

	@Test
	public void theMappedSelectionKeepsItsSwitches() {
		PointSelector point = new PointSelector();
		Goal twice = point.x.unifies(1).and(point.y.unifies(2));
		assertThat(all(Query.of(twice.or(twice)).select(point).stream())).hasSize(1);
		assertThat(all(Query.of(twice.or(twice)).select(point).all().stream())).hasSize(2);
	}

	/** The rows stream whatever the question answers; reading a cell that is not ground is what refuses. */
	@Test
	public void aCellThatIsNotGroundRefusesWhereTheMapperReadsIt() {
		PointSelector point = new PointSelector();
		assertThat(all(Query.of(point.x.unifies(1)).select(point).rows())).hasSize(1);
		assertThatThrownBy(() -> all(Query.of(point.x.unifies(1)).select(point).stream()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("not ground")
				.hasMessageContaining(point.y.toString());
		assertThat(point.mapped.get()).isEqualTo(1);
	}

	/** The rows stay in hand beside the mapping: a second function over them reads what the mapper does not. */
	@Test
	public void rowsAreTheSameSelectionUnmapped() {
		PointSelector point = new PointSelector();
		List<Integer> xs = all(Query.of(
				point.x.unifies(1).and(point.y.unifies(2))
						.or(point.x.unifies(3).and(point.y.unifies(4))))
				.select(point).rows().map(row -> row.get(point.x)));
		assertThat(xs).containsExactlyInAnyOrder(1, 3);
		assertThat(point.mapped.get()).isZero();
	}

	/** The refusal shows every cell by the name the selector gives its key. */
	@Test
	public void theRefusalNamesTheCellsAsTheSelectorDoes() {
		NamedPointSelector point = new NamedPointSelector();
		assertThatThrownBy(() -> all(Query.of(point.x.unifies(1)).select(point).stream()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("not ground")
				.hasMessageContaining("(x: {1}, y: _.");
	}

	@Test
	public void theVariablesReadingIsTheSolutionItself() {
		Unifiable<Integer> x = lvar();
		List<Row> rows = all(Query.of(x.unifies(1)).select(x).stream());
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).get(x)).isEqualTo(1);
	}
}
