package org.clauseway.logic.unification.terms;

// ABOUTME: The door from a term to a unifiable: a variable or a value passes through as itself, a reified
// ABOUTME: name refuses — an answer's free cell has no place in a goal.

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.solving.Query;
import org.junit.Test;

public class TermTest {

	@Test
	public void aVariableIsItsOwnUnifiable() {
		Term<Integer> x = lvar();
		assertThat(x.unifiable()).isSameAs(x);
	}

	@Test
	public void aValueIsItsOwnUnifiable() {
		Term<Integer> three = lval(3);
		assertThat(three.unifiable()).isSameAs(three);
	}

	@Test
	public void aReifiedNameRefuses() {
		Unifiable<Integer> free = lvar();
		Term<Integer> any = Query.of(Goal.success()).solve(free).findFirst().get();
		assertThat(any.asReified()).isPresent();
		assertThatThrownBy(any::unifiable)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("reified");
	}
}
