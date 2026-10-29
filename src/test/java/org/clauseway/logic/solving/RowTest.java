package org.clauseway.logic.solving;

// ABOUTME: Pins what a solution's cell is when the answer is not a value: the cell as the
// ABOUTME: reifier rendered it, with the residuals about it, and the refusals that say so.

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.clauseway.logic.nogoods.Exclusion.exclude;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.clauseway.logic.constraints.Constrained;
import org.clauseway.logic.unification.terms.Any;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class RowTest {

	private static <D> List<D> all(Stream<D> stream) {
		try (Stream<D> rows = stream) {
			return rows.collect(Collectors.toList());
		}
	}

	private static Row one(Selection<Row> selection) {
		List<Row> rows = all(selection.stream());
		assertThat(rows).hasSize(1);
		return rows.get(0);
	}

	/** An exclusion over a free variable answers under a nogood; the cell carries that nogood. */
	@Test
	public void aCellUnderAResidualIsConstrainedByIt() {
		Unifiable<Integer> x = lvar();
		Row row = one(Query.of(exclude(x.unifies(1))).select(x));
		Reified<Integer> cell = row.cell(x);
		assertThat(cell).isInstanceOf(Constrained.class);
		assertThat(cell.isGround()).isFalse();
		assertThat(cell.toString()).contains("¬(").contains("1");
	}

	/** A residual about one cell does not ride the other. */
	@Test
	public void aResidualRidesOnlyTheCellsItMentions() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Row row = one(Query.of(exclude(x.unifies(1)).and(y.unifies(2))).select(x, y));
		assertThat(row.cell(x)).isInstanceOf(Constrained.class);
		assertThat(row.get(y)).isEqualTo(2);
	}

	/** A cell no residual mentions is the slot name the reifier gave it. */
	@Test
	public void aFreeCellIsItsSlotName() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Row row = one(Query.of(x.unifies(1)).select(x, y));
		assertThat(row.cell(y)).isInstanceOf(Any.class);
	}

	/** Reading a constrained cell as a value refuses by column, rendering the row whole under its residuals. */
	@Test
	public void aConstrainedCellRefusesToBeAValueWithTheWholeRow() {
		Unifiable<Integer> x = lvar();
		Row row = one(Query.of(exclude(x.unifies(1))).select(x));
		assertThatThrownBy(() -> row.get(x))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("not ground at [" + x + "]")
				.hasMessageContaining(row.toString());
		assertThatThrownBy(row::ground)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("not ground at [" + x + "]")
				.hasMessageContaining(row.toString());
	}

	/** Reading a free cell as a value refuses by its slot name. */
	@Test
	public void aFreeCellRefusesToBeAValueByItsSlotName() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Row row = one(Query.of(x.unifies(1)).select(x, y));
		String rendering = row.cell(y).toString();
		assertThatThrownBy(() -> row.get(y))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining(rendering);
		assertThatThrownBy(row::ground)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining(y.toString())
				.hasMessageContaining(rendering);
	}

	/** The refusal renders the whole solution: every cell by its key, in the select's order, under the residuals. */
	@Test
	public void theRefusalRendersTheWholeSolutionUnderItsResiduals() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Unifiable<Integer> z = lvar();
		Row row = one(Query.of(x.unifies(1).and(exclude(y.unifies(2)))).select(x, y, z));
		assertThat(row.toString())
				.startsWith("(" + x + ": {1}, " + y + ": _.")
				.contains(", " + z + ": _.")
				.contains(") : ¬(")
				.contains("2");
		assertThatThrownBy(row::ground)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("not ground")
				.hasMessageContaining(row.toString());
	}

	/** A ground solution renders as its cells alone. */
	@Test
	public void aDecidedSolutionRendersWithoutResiduals() {
		Unifiable<Integer> x = lvar();
		Row row = one(Query.of(x.unifies(1)).select(x));
		assertThat(row.toString()).isEqualTo("(" + x + ": {1})");
	}
}
