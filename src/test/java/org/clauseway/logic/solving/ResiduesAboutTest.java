package org.clauseway.logic.solving;

// ABOUTME: Pins the key crossing: what a world knows about an anchor is read
// ABOUTME: after bound names resolve, so a bound variable keys as its value does.

import static org.assertj.core.api.Assertions.assertThat;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import org.clauseway.logic.finitedomain.Ints;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class ResiduesAboutTest {

	@Test
	public void aComparisonWithABoundVariableKeysAsItsValueDoes() {
		// lss(i, greater) with i bound to 0 is the same knowledge about
		// greater as lss(0, greater): the key must not drop it as coupled
		// to an outside name
		Unifiable<Integer> i = lvar();
		Unifiable<Integer> greater = lvar();
		Knowledge viaVariable = Query.of(i.unifies(0).and(Ints.lss(i, greater))).stream().iterator().next();
		Unifiable<Integer> other = lvar();
		Knowledge viaValue = Query.of(Ints.lss(lval(0), other)).stream().iterator().next();

		Residues keyedVariable = Residues.about(viaVariable, greater).ground()._2;
		Residues keyedValue = Residues.about(viaValue, other).ground()._2;

		assertThat(keyedValue.isTrue()).isFalse();
		assertThat(keyedVariable).isEqualTo(keyedValue);
	}
}
