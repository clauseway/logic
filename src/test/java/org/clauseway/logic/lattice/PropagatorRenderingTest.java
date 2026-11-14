package org.clauseway.logic.lattice;

// ABOUTME: Pins how a propagator atom renders: its name applied to its watched
// ABOUTME: terms, the way a condition or a refusal shows it to a reader.

import static org.assertj.core.api.Assertions.assertThat;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import org.clauseway.logic.constraints.Posting;
import org.clauseway.logic.constraints.store.Atom;
import org.clauseway.logic.finitedomain.Ints;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class PropagatorRenderingTest {

	/** A propagator renders as {@code name(term, term)}, never through the collection's own rendering. */
	@Test
	public void rendersAsNameAppliedToWatchedTerms() {
		Unifiable<Integer> x = lvar();
		Atom<?> lss = ((Posting.Activation) Ints.lss(lval(1), x)).getItem();
		assertThat(lss.toString()).isEqualTo("lss({1}, " + x + ")");
	}
}
