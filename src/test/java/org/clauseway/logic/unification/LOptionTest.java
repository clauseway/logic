package org.clauseway.logic.unification;

// ABOUTME: LOption is the engine's optional: some(v) and none() are structures the
// ABOUTME: unifier decomposes, walks and reifies; absence is a term, never a null.

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.clauseway.logic.goals.Matche.loption;
import static org.clauseway.logic.goals.Matche.matche;
import static org.clauseway.logic.unification.structures.LOption.none;
import static org.clauseway.logic.unification.structures.LOption.some;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Logic;
import org.clauseway.logic.solving.Query;
import org.clauseway.logic.unification.structures.LOption;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class LOptionTest {

	@Test
	public void someDestructuresAgainstSome() {
		Unifiable<String> inner = lvar();
		List<String> answers = Query.of(some(inner).unifies(some(lval("a")))).solve(inner)
				.map(r -> r.get())
				.collect(Collectors.toList());
		assertThat(answers).containsExactly("a");
	}

	@Test
	public void noneIsAValueEqualToItselfOnly() {
		Unifiable<LOption<String>> x = lvar();
		assertThat(Query.of(x.unifies(none()).and(x.unifies(none()))).solve(x).count()).isEqualTo(1);
		assertThat(Query.of(x.unifies(none()).and(x.unifies(some(lval("a"))))).solve(x).count()).isZero();
		assertThat(Query.of(x.unifies(some(lvar())).and(x.unifies(none()))).solve(x).count()).isZero();
	}

	@Test
	public void aGroundOptionReifiesToAJavaOptional() {
		Unifiable<LOption<Integer>> x = lvar();
		Unifiable<Integer> y = lvar();
		List<Optional<Integer>> answers = Query.of(x.unifies(some(y)).and(y.unifies(2))).solve(x)
				.map(r -> r.get().toOptional())
				.collect(Collectors.toList());
		assertThat(answers).containsExactly(Optional.of(2));
		assertThat(Query.of(x.unifies(none())).solve(x)
				.map(r -> r.get().toOptional())
				.collect(Collectors.toList())).containsExactly(Optional.empty());
	}

	@Test
	public void someWithAFreeInnerReifiesAsAHoleAndRefusesToOptional() {
		Unifiable<LOption<Integer>> x = lvar();
		Reified<LOption<Integer>> hole = Query.of(x.unifies(some(lvar()))).solve(x).findFirst().get();
		assertThat(hole.isGround()).isFalse();
		assertThatThrownBy(() -> hole.get().toOptional())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("unbound");
		assertThat(Query.of(x.unifies(some(lval(1)))).solve(x).findFirst().get().isGround()).isTrue();
	}

	@Test
	public void matcheTakesOneArmPerConstructor() {
		Unifiable<LOption<String>> voyage = lvar();
		Unifiable<String> status = lvar();
		List<String> onSome = Query.of(voyage.unifies(some(lval("0123")))
				.and(matche(voyage,
						loption(() -> status.unifies("port side")),
						loption(v -> status.unifies(v))))).solve(status)
				.map(r -> r.get())
				.collect(Collectors.toList());
		assertThat(onSome).containsExactly("0123");

		Unifiable<LOption<String>> absent = lvar();
		List<String> onNone = Query.of(absent.unifies(none())
				.and(matche(absent,
						loption(() -> status.unifies("port side")),
						loption(v -> status.unifies(v))))).solve(status)
				.map(r -> r.get())
				.collect(Collectors.toList());
		assertThat(onNone).containsExactly("port side");
	}

	@Test
	public void aFreeOptionUnderMatcheEnumeratesBothShapes() {
		Unifiable<LOption<String>> o = lvar();
		List<String> shapes = Query.of(matche(o,
				loption(() -> Goal.success()),
				loption(v -> v.unifies("x")))).solve(o)
				.map(r -> r.get().toString())
				.collect(Collectors.toList());
		assertThat(shapes).containsExactly("None", "Some({x})");
	}
}
