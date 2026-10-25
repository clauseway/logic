package org.clauseway.logic.solving;

// ABOUTME: One solution of a select: Reified cells keyed by the selected
// ABOUTME: variables, under the derivation's Condition -- a view over Answer.

import org.clauseway.functional.tuples.Tuple;
import org.clauseway.logic.constraints.Constrained;
import org.clauseway.logic.constraints.store.Atom;
import org.clauseway.logic.constraints.store.Theory;
import org.clauseway.logic.unification.MiniKanren;
import org.clauseway.logic.unification.terms.Name;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.vavr.collection.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * One row of a {@link Selection}: the selected variables' cells from one
 * solution, read by the variable itself — the key carries the type, the
 * cell comes back in output vocabulary. A decided cell is a value
 * ({@code isVal()}/{@code get()}); a cell the solution did not decide is
 * the slot name the reifier gave it, under the answer's residuals about
 * it — a fact of the answer, never smoothed into an empty.
 * {@link #getCondition} is the knowledge this row holds under;
 * {@link #answer} is the whole boundary artifact for anything that ships
 * rows onward.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Solution {

	private final java.util.List<Unifiable<?>> keys;
	private final Answer<Unifiable<?>> answer;

	/**
	 * The cell at {@code key}, stated as the reifier would: a value, a slot
	 * name, or that name under the residuals about it ({@link Constrained})
	 * when the answer is one region. A row folded from several regions
	 * shows its name alone; the regions are {@link #getCondition()}.
	 * Refuses a variable the select did not name.
	 */
	@SuppressWarnings("unchecked")
	public <T> Reified<T> get(Term<T> key) {
		int at = keys.indexOf(key);
		if (at < 0) {
			throw new IllegalArgumentException(
					"not a selected variable: " + key + " — this row's columns are " + keys);
		}
		return about((Reified<T>) ((Tuple) answer.getReified().get()).get(at + 1), answer.getCondition());
	}

	/** The cell under the one region's residuals that mention a name in it; the cell alone when none does. */
	private static <T> Reified<T> about(Reified<T> cell, Condition condition) {
		if (condition.conjuncts().size() != 1) {
			return cell;
		}
		Set<Name<?>> names = MiniKanren.namesIn(cell).collect(Collectors.toSet());
		List<Atom<?>> about = List.empty();
		for (Theory<?> theory : condition.conjuncts().get(0).getTheories().values()) {
			for (Atom<?> atom : theory.atoms()) {
				if (atom.watched().exists(term -> MiniKanren.namesIn(term).anyMatch(names::contains))) {
					about = about.append(atom);
				}
			}
		}
		return about.isEmpty() ? cell : Constrained.of(cell, about);
	}

	/**
	 * This solution with every cell decided, or a refusal rendering the
	 * first one that is not: the gate a reading into Java passes through,
	 * since a Java value has no place for a slot name or a residual.
	 */
	public Solution decided() {
		for (Unifiable<?> key : keys) {
			Reified<?> cell = get(key);
			if (!cell.isGround()) {
				throw new IllegalStateException("the cell at " + key + " is not decided, it reifies as " + cell
						+ " — a reading into Java needs a value in every selected cell");
			}
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

	@Override
	public String toString() {
		return answer.toString();
	}
}
