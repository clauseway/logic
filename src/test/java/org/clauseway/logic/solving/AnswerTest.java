package org.clauseway.logic.solving;

// ABOUTME: Pins the Answer artifact, Call's dual: value equality over the
// ABOUTME: whole triple and unconditional() dropping the guard to ONE.

import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.tuples.Tuple;
import org.clauseway.logic.unification.terms.Reified;
import org.junit.Test;

public class AnswerTest {

	private static final Condition GUARDED = Condition.of(Span.factor(3L, 6L));

	@Test
	public void equalityIsTheWholeTriple() {
		Answer<String> one = Answer.of("rel", (Reified<?>) lval(Tuple.of("alice", "bob")), Condition.ONE);
		Answer<String> two = Answer.of("rel", (Reified<?>) lval(Tuple.of("alice", "bob")), Condition.ONE);
		assertThat(one).isEqualTo(two);
		assertThat(one.hashCode()).isEqualTo(two.hashCode());
		assertThat(one).isNotEqualTo(
				Answer.of("rel", (Reified<?>) lval(Tuple.of("alice", "bob")), GUARDED));
		assertThat(one).isNotEqualTo(
				Answer.of("other", (Reified<?>) lval(Tuple.of("alice", "bob")), Condition.ONE));
	}

	@Test
	public void unconditionalDropsTheGuard() {
		Answer<String> guarded = Answer.of("rel", (Reified<?>) lval(Tuple.of("alice")), GUARDED);
		Answer<String> stripped = guarded.unconditional();
		assertThat(stripped.getCondition()).isSameAs(Condition.ONE);
		assertThat(stripped.getRelation()).isEqualTo("rel");
		assertThat(stripped.getReified()).isEqualTo(guarded.getReified());
	}

	@Test
	public void unconditionalOfOneIsItself() {
		Answer<String> plain = Answer.of("rel", (Reified<?>) lval(Tuple.of("alice")), Condition.ONE);
		assertThat(plain.unconditional()).isEqualTo(plain);
	}
}
