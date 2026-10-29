package org.clauseway.logic.solving;

// ABOUTME: One row of a select: cells keyed by the selected variables, under the derivation's
// ABOUTME: Condition -- a view over Answer; get(key) is the gate into Java, cell(key) the reifier's reading.

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.logic.constraints.Constrained;
import org.clauseway.logic.constraints.store.Atom;
import org.clauseway.logic.unification.MiniKanren;
import org.clauseway.logic.unification.terms.Name;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.vavr.collection.List;

/**
 * One row of a {@link Selection}: the selected variables' cells from one
 * solution, read by the variable itself — the key carries the type.
 * {@link #get} is the reading into Java: the cell's value, or a refusal
 * when the cell is not ground, since a Java value has no place for a slot
 * name or a residual. {@link #cell} is the reifier's reading: a value, the
 * slot name the reifier gave a free cell, or that name under the answer's
 * residuals about it — a symbolic answer over many ground ones, never
 * smoothed into an empty. {@link #getCondition} is the knowledge this row
 * holds under; {@link #answer} is the whole boundary artifact for anything
 * that ships rows onward. The row renders whole, shaped as the select
 * named it: {@code (name: cell, ...)}, under the residuals when there are any.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Row {

	private final java.util.List<Unifiable<?>> keys;
	private final java.util.List<String> names;
	private final Answer<Unifiable<?>> answer;

	/**
	 * The value at {@code key}: the gate into Java. Refuses a cell that is
	 * not ground, rendering the row whole, and a variable the select did
	 * not name.
	 */
	public <T> T get(Unifiable<T> key) {
		int at = at(key);
		Reified<T> cell = slot(at);
		if (!cell.isGround()) {
			throw new IllegalStateException(notGround(Collections.singletonList(names.get(at))));
		}
		return cell.get();
	}

	/**
	 * The cell at {@code key}, stated as the reifier would: a value, a slot
	 * name, or that name under the residuals about it ({@link Constrained})
	 * when the answer is one region. A row folded from several regions
	 * shows its name alone; the regions are {@link #getCondition()}.
	 * Refuses a variable the select did not name.
	 */
	public <T> Reified<T> cell(Unifiable<T> key) {
		return about(slot(at(key)), answer.getCondition());
	}

	private int at(Unifiable<?> key) {
		int at = keys.indexOf(key);
		if (at < 0) {
			throw new IllegalArgumentException(
					"not a selected variable: " + key + " — this row's columns are " + keys);
		}
		return at;
	}

	@SuppressWarnings("unchecked")
	private <T> Reified<T> slot(int at) {
		return (Reified<T>) ((Tuple) answer.getReified().get()).get(at + 1);
	}

	private String notGround(java.util.List<String> symbolic) {
		return "not ground at " + symbolic + ": " + this
				+ " — a reading into Java needs a value in every selected cell";
	}

	/** The cell under the one region's residuals that mention a name in it; the cell alone when none does. */
	private static <T> Reified<T> about(Reified<T> cell, Condition condition) {
		if (condition.conjuncts().size() != 1) {
			return cell;
		}
		Set<Name<?>> mentioned = MiniKanren.namesIn(cell).collect(Collectors.toSet());
		List<Atom<?>> about = atoms(condition.conjuncts().get(0))
				.filter(atom -> atom.watched().exists(term -> MiniKanren.namesIn(term).anyMatch(mentioned::contains)))
				.collect(List.collector());
		return about.isEmpty() ? cell : Constrained.of(cell, about);
	}

	/** One region's residuals, flat. */
	private static Stream<Atom<?>> atoms(Residues region) {
		return region.getTheories().values().toJavaStream()
				.flatMap(t -> t.atoms().toJavaStream());
	}

	/**
	 * This row with every cell ground, or a refusal naming the cells that
	 * are not and rendering the row whole: {@link #get}'s check over every
	 * cell at once, for a reading that wants the whole row or nothing.
	 */
	public Row ground() {
		java.util.List<String> symbolic = IntStream.range(0, keys.size())
				.filter(at -> !slot(at).isGround())
				.mapToObj(names::get)
				.collect(Collectors.toList());
		if (!symbolic.isEmpty()) {
			throw new IllegalStateException(notGround(symbolic));
		}
		return this;
	}

	public Condition getCondition() {
		return answer.getCondition();
	}

	/** The row as the seam artifact, whole. */
	public Answer<Unifiable<?>> answer() {
		return answer;
	}

	/** The cells by name, in the select's order, under the residuals when there are any: regions ∨-joined, each ∧-joined. */
	@Override
	public String toString() {
		String cells = IntStream.range(0, keys.size())
				.mapToObj(at -> names.get(at) + ": " + slot(at))
				.collect(Collectors.joining(", ", "(", ")"));
		Condition condition = answer.getCondition();
		return condition.isOne() ? cells : cells + " : "
				+ condition.conjuncts().map(region ->
						atoms(region)
								.map(Atom::toString)
								.collect(Collectors.joining(" ∧ ")))
				.mkString(" ∨ ");
	}
}
