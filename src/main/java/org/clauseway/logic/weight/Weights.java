package org.clauseway.logic.weight;

// ABOUTME: The weighted-inference front door: factor injects a per-choice weight
// ABOUTME: (⊗ into the running store), solve ⊕-folds the per-answer stores to a total.

import org.clauseway.functional.algebra.BoundedSemiring;
import org.clauseway.functional.algebra.ClosedSemiring;
import org.clauseway.functional.algebra.Semiring;
import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Scheduler;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.Query;
import org.clauseway.logic.constraints.Constraints;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.goals.optimizer.Bounded;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.functional.tuples.Tuple2;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Weights {

	/**
	 * Succeeds once, multiplying {@code weight} into {@code ring}'s running value
	 * in the ambient {@link SemiringStore}. ⊗ = along a derivation; because the
	 * package is immutable, branches carry independent weights. Inert when no
	 * store rides the package (weighing off), like a tracer-less named goal.
	 */
	public static <S> Goal factor(Semiring<S> ring, S weight) {
		return Bounded.of(1, Goal.goal(s -> Cont.just(
						s.updateStore(SemiringStore.class,
								store -> store.with(ring, ring.times(store.get(ring), weight)))))
				.named("factor"));
	}

	/**
	 * Runs {@code goal} to exhaustion under the product semiring, ⊕-folding the
	 * per-answer stores into one total. The ring set is fixed here (the seed is
	 * {@code product.one()}), so every store in flight is complete and
	 * {@link #factor} only ever updates an existing entry. Eager, like counting:
	 * a weighted total is unknown until the search completes.
	 */
	public static SemiringStore solve(Goal goal, Semiring<SemiringStore> product,
			Function<Fiber<Nothing>, Scheduler<Nothing>> factory) {
		try (Stream<Knowledge> worlds = Query.of(goal).weighted(product).on(factory).stream()) {
			return worlds.map(w -> w.getStore(SemiringStore.class))
					.reduce(product.zero(), product::plus);
		}
	}

	/**
	 * Each answer paired with its own branch's weight — the per-answer view the
	 * fold in {@link #solve} discards. Lets a caller ask which branch is
	 * cheapest, not just the folded extremum.
	 */
	public static <T> Stream<Tuple2<Reified<T>, SemiringStore>> solveEach(Goal goal, Unifiable<T> out,
			Semiring<SemiringStore> product, Function<Fiber<Nothing>, Scheduler<Nothing>> factory) {
		return each(Query.of(goal).weighted(product).on(factory), out);
	}

	/**
	 * Weighted solve with STREAMING tabling: tabled calls thread their weights,
	 * the answer cell folds by ⊕, and recursion (cyclic included) terminates
	 * because boundedness (a* = 1) makes re-derivation stationary — min-plus,
	 * Viterbi, boolean. The sibling {@code solveClosed} (star) is the escape for
	 * the unbounded-or-non-idempotent semirings (probability, provenance);
	 * {@link #solveEach} without a capability is the plain non-tabling per-answer
	 * solve. Naming the capability at the call site keeps the choice of strategy
	 * explicit. With no tabled goal the weighted table simply sits unused.
	 */
	public static <T> Stream<Tuple2<Reified<T>, SemiringStore>> solveBounded(Goal goal, Unifiable<T> out,
			BoundedSemiring<SemiringStore> product, Function<Fiber<Nothing>, Scheduler<Nothing>> factory) {
		return each(Query.of(goal).weighted(product).on(factory), out);
	}

	/**
	 * Weighted solve with CLOSED (star) tabling: a closed but non-idempotent (or
	 * unbounded) semiring whose values cannot stream. Explore runs as plain
	 * presence tabling; the real value is deferred and dropped as an exploration
	 * fragment, then summed by the star at each sealed closure and replayed
	 * untagged to the collector (star-tabling.md §4). A bounded semiring is a
	 * legal argument too -- you pay O(n^3) for the degenerate a* = 1.
	 */
	public static <T> Stream<Tuple2<Reified<T>, SemiringStore>> solveClosed(Goal goal, Unifiable<T> out,
			ClosedSemiring<SemiringStore> ring, Function<Fiber<Nothing>, Scheduler<Nothing>> factory) {
		Query query = Query.of(goal).weighted(ring).on(factory);
		return Query.harvest(query.run()
						.flatMap(s -> Constraints.reify(s, out)
								.map(answer -> Tuple.of(answer,
										s.getStore(SemiringStore.class),
										s.getStores().get(Fragment.class).isDefined()))),
				query.factory())
				// keep only finalized (untagged) answers; exploration fragments drop
				.filter(triple -> !triple._3)
				.map(triple -> Tuple.of(triple._1, triple._2));
	}

	/** The per-answer pairing: reify {@code out} in-fiber, ride the world's store along. */
	private static <T> Stream<Tuple2<Reified<T>, SemiringStore>> each(Query query, Unifiable<T> out) {
		return Query.harvest(query.run()
						.flatMap(s -> Constraints.reify(s, out)
								.map(answer -> Tuple.of(answer, s.getStore(SemiringStore.class)))),
				query.factory());
	}
}
