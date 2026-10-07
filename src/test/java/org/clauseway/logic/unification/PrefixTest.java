package org.clauseway.logic.unification;

// ABOUTME: Pins Prefix.revalidate's trichotomy when a prefix minted in one state
// ABOUTME: is applied in a later one where its variables have moved.

import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.tuples.Tuple;
import java.util.Optional;
import org.clauseway.logic.unification.terms.LVar;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class PrefixTest {

	/** The unifier's prefix for {@code lhs = rhs} over {@code s}. */
	private static <T> Prefix minted(Substitutions s, Term<T> lhs, Term<T> rhs) {
		return MiniKanren.unifyPrefix(s, lhs, rhs).ground().get();
	}

	private static <T> LVar<T> var(Unifiable<T> v) {
		return v.asVar().get();
	}

	@Test
	public void aPairWhoseValueWalksToTheVariablesBindingIsDroppedNotRefused() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Prefix aliasing = minted(Substitutions.empty(), x, y);

		// meanwhile both sides were bound to the same value
		Substitutions later = Substitutions.empty()
				.extend(var(x), lval(5))
				.extend(var(y), lval(5));

		Optional<Prefix> kept = aliasing.revalidate(later).ground();
		assertThat(kept.isPresent()).as("x = y agrees with x = 5, y = 5").isTrue();
		assertThat(kept.get().isEmpty()).isTrue();
	}

	@Test
	public void aStructuredPairWhoseMembersWalkToTheBoundStructureIsDroppedNotRefused() {
		Unifiable<Object> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Prefix structural = minted(Substitutions.empty(), x, lval(Tuple.of(y, 1)));

		Substitutions later = Substitutions.empty()
				.extend(var(x), lval(Tuple.of(lval(5), 1)))
				.extend(var(y), lval(5));

		Optional<Prefix> kept = structural.revalidate(later).ground();
		assertThat(kept.isPresent()).as("x = (y, 1) agrees with x = (5, 1), y = 5").isTrue();
		assertThat(kept.get().isEmpty()).isTrue();
	}

	@Test
	public void aPairThePackageAlreadyHoldsIsDroppedNotBoundToItself() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Prefix aliasing = minted(Substitutions.empty(), x, y);

		Substitutions later = aliasing.appliedTo(Substitutions.empty());

		Optional<Prefix> kept = aliasing.revalidate(later).ground();
		assertThat(kept.isPresent()).isTrue();
		assertThat(kept.get().isEmpty()).as("re-applying x = y adds nothing").isTrue();
	}

	@Test(timeout = 2000)
	public void reapplyingAPairThePackageAlreadyHoldsKeepsWalkTerminating() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Prefix aliasing = minted(Substitutions.empty(), x, y);
		Substitutions later = aliasing.appliedTo(Substitutions.empty());

		Substitutions extended = later.extended(aliasing).ground().get()._1;

		assertThat(extended.walk(x)).isEqualTo(y);
		assertThat(extended.walk(y)).isEqualTo(y);
	}

	@Test(timeout = 2000)
	public void aPairAliasedTheOtherWayMeanwhileDoesNotCloseACycle() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Prefix xToY = minted(Substitutions.empty(), x, y);

		// meanwhile the package aliased them the other way round
		Substitutions later = Substitutions.empty().extend(var(y), x);

		Substitutions extended = later.extended(xToY).ground().get()._1;

		assertThat(extended.walk(x)).isEqualTo(extended.walk(y));
	}

	@Test
	public void aPairWhoseVariableIsBoundAndWhoseValueIsStillOpenBindsTheValue() {
		Unifiable<Integer> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Prefix aliasing = minted(Substitutions.empty(), x, y);

		// meanwhile x was bound; y is still open, so x = y now says y = 5
		Substitutions later = Substitutions.empty().extend(var(x), lval(5));

		Optional<Prefix> kept = aliasing.revalidate(later).ground();
		assertThat(kept.isPresent()).as("x = y agrees with x = 5").isTrue();
		assertThat(kept.get().appliedTo(later).walk(y)).isEqualTo(lval(5));
	}

	@Test
	public void aStructuredPairWithOpenMembersOnBothSidesBindsThem() {
		Unifiable<Object> x = lvar("x");
		Unifiable<Integer> y = lvar("y");
		Unifiable<Integer> z = lvar("z");
		Prefix structural = minted(Substitutions.empty(), x, lval(Tuple.of(y, 1)));

		// meanwhile x was bound to a structure with its own open member
		Substitutions later = Substitutions.empty().extend(var(x), lval(Tuple.of(5, z)));

		Optional<Prefix> kept = structural.revalidate(later).ground();
		assertThat(kept.isPresent()).as("x = (y, 1) agrees with x = (5, z)").isTrue();
		Substitutions applied = kept.get().appliedTo(later);
		assertThat(applied.walk(y)).isEqualTo(lval(5));
		assertThat(applied.walk(z)).isEqualTo(lval(1));
	}

	@Test
	public void aPairBoundToADifferentValueIsAContradiction() {
		Unifiable<Integer> x = lvar("x");
		Prefix five = minted(Substitutions.empty(), x, lval(5));

		Substitutions later = Substitutions.empty().extend(var(x), lval(6));

		assertThat(five.revalidate(later).ground().isPresent()).isFalse();
	}
}
