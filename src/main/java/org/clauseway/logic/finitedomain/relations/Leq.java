package org.clauseway.logic.finitedomain.relations;

// ABOUTME: The leq schema: less ≤ more — bounds narrow both ways; doomed the
// ABOUTME: moment a ground comparison already violates the order.

import org.clauseway.logic.constraints.store.Theory;
import org.clauseway.logic.finitedomain.Domain;
import org.clauseway.logic.finitedomain.FiniteDomainConstraints;
import org.clauseway.logic.finitedomain.relations.Operators.VarWithDomain;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.lattice.Propagator;
import org.clauseway.logic.lattice.Verdict;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.vavr.collection.Array;
import java.util.Arrays;
import java.util.Comparator;

/**
 * The propagator for {@code less <= more} under a {@link Comparator}, posted
 * through {@link org.clauseway.logic.finitedomain.FiniteDomain#leq}. When both
 * terms are ground the comparison decides the verdict outright; otherwise the
 * domain of {@code less} is cut to at most the upper bound of {@code more} and
 * the domain of {@code more} to at least the lower bound of {@code less}, failing
 * if either becomes empty. {@link #doomed} reports a ground pair that violates
 * the order.
 */
public final class Leq extends Propagator<FiniteDomainConstraints> {

	private final Comparator<Object> order;

	@SuppressWarnings("unchecked")
	public Leq(Term<?> less, Term<?> more, Comparator<?> order) {
		this(Array.of(less, more), (Comparator<Object>) order);
	}

	private Leq(Array<? extends Term<?>> terms, Comparator<Object> order) {
		super(terms);
		this.order = order;
	}

	@Override
	public Verdict propagate(Knowledge state) {
		return Operators.gated(order, vds -> leqVerdict(vds.get(0), vds.get(1), order))
				.apply(watchedTerms(), state);
	}

	@Override
	public Propagator<FiniteDomainConstraints> watching(Array<? extends Term<?>> terms) {
		return new Leq(terms, order);
	}

	@Override
	public FiniteDomainConstraints empty() {
		return FiniteDomainConstraints.empty();
	}

	@Override
	public boolean doomed(Knowledge state) {
		return Operators.cmpOrder(state.substitution(),
				watchedTerms().get(0), watchedTerms().get(1), c -> c <= 0, order) == 0;
	}

	@Override
	public String name() {
		return "leq";
	}

	@Override
	public Class<? extends FiniteDomainConstraints> getFactorClass() {
		return FiniteDomainConstraints.class;
	}

	@SuppressWarnings("unchecked")
	static <T> Verdict leqVerdict(VarWithDomain<T> lss, VarWithDomain<T> mor, Comparator<T> order) {
		if (lss.getUnifiable().isVal() && mor.getUnifiable().isVal()) {
			// ground: the order decides exactly, nothing left to watch
			return order.compare(lss.getUnifiable().get(), mor.getUnifiable().get()) <= 0 ?
					Verdict.subsumed() : Verdict.fail();
		}
		Domain<T> lessDom = lss.<T> getDomain().atMost(mor.<T> getDomain().upper());
		Domain<T> moreDom = mor.<T> getDomain().atLeast(lss.<T> getDomain().lower());
		if (lessDom.isEmpty() || moreDom.isEmpty()) {
			return Verdict.fail();
		}
		return Verdict.update((state, theory) -> DomainUpdate.narrowAll(state,
				(Theory<FiniteDomainConstraints>) theory,
				Arrays.asList(
						VarWithDomain.of(lss.getUnifiable(), lessDom),
						VarWithDomain.of(mor.getUnifiable(), moreDom))));
	}
}
