package org.clauseway.logic.solving;

// ABOUTME: One solution row of a select: Reified cells keyed by the selected
// ABOUTME: variables, under the derivation's Condition -- a view over Answer.

import org.clauseway.functional.tuples.Tuple;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * One row of a {@link Selection}: the selected variables' cells from one
 * solution, read by the variable itself — the key carries the type, the
 * cell comes back in output vocabulary. A decided cell is a value
 * ({@code isVal()}/{@code get()}), a cell the solution left open is its
 * Any — wideness is a fact of the answer, never smoothed into an empty.
 * {@link #getCondition} is the knowledge this row holds under;
 * {@link #answer} is the whole boundary artifact for anything that ships
 * rows onward.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Row {

	private final List<Unifiable<?>> keys;
	private final Answer<Unifiable<?>> answer;

	/** The cell at {@code key}; refuses a variable the select did not name. */
	@SuppressWarnings("unchecked")
	public <T> Reified<T> get(Term<T> key) {
		int at = keys.indexOf(key);
		if (at < 0) {
			throw new IllegalArgumentException(
					"not a selected variable: " + key + " — this row's columns are " + keys);
		}
		return (Reified<T>) ((Tuple) answer.getReified().get()).get(at + 1);
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
