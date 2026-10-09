package org.clauseway.logic.goals.optimizer;

// ABOUTME: Pins the ambient ordering layer: ascending sort within barrier-delimited
// ABOUTME: segments, derived orders through combinators, and ambient-solve equivalence.

import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.clauseway.functional.algebra.Semirings;
import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.interpreter.Scope;
import org.clauseway.functional.fibers.interpreter.StepListener;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.logic.aggregate.Aggregate;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Conjunction;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.goals.Logic;
import org.clauseway.logic.tabling.Tabled;
import org.clauseway.logic.tabling.Tabling;
import org.clauseway.logic.tabling.table.Table;
import org.clauseway.logic.unification.Substitutions;
import org.clauseway.logic.unification.structures.LList;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.functional.tuples.Tuple1;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.Value;
import org.clauseway.logic.unification.terms.Term;
import org.junit.Test;

public class OrderingOptimizerTest {

	@Value
	private static class FixedOrder implements Goal, Bounded {
		long n;

		@Override
		public long answers(Substitutions s) {
			return n;
		}

		@Override
		public Cont<Knowledge, Nothing> apply(Knowledge s) {
			return Cont.just(s);
		}
	}

	private static Goal opaque() {
		return s -> Cont.just(s);
	}

	/** Prices 5 blind, 1 sighted — pins that the pass prices with the package. */
	@Value
	private static class StoreSighted implements Goal, Bounded {
		@Override
		public long answers(Substitutions s) {
			return 5;
		}

		@Override
		public long answers(Knowledge p) {
			return 1;
		}

		@Override
		public Cont<Knowledge, Nothing> apply(Knowledge s) {
			return Cont.just(s);
		}
	}

	@Test
	public void aNegativeBoundRefusesLoudly() {
		// a bound is a COUNT: the pricer's saturating arithmetic and segment
		// sort both assume non-negatives, so a lying estimator refuses by name
		Goal liar = new FixedOrder(-5);
		assertThatThrownBy(() -> liar.and(new FixedOrder(3)).accept(new OrderingOptimizer()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("-5");
	}

	@Test
	public void pricesWithThePackageNotJustTheSubstitution() {
		Goal sighted = new StoreSighted();
		Goal b3 = new FixedOrder(3);
		Goal sorted = b3.and(sighted).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(sighted, b3);
	}

	@Test
	public void sortsSegmentsAscendingAroundBarriers() {
		Goal b5 = new FixedOrder(5), b1 = new FixedOrder(1), b3 = new FixedOrder(3), b2 = new FixedOrder(2);
		Goal barrier = opaque();
		Goal sorted = b5.and(b1).and(barrier).and(b3).and(b2)
				.accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b1, b5, barrier, b2, b3);
	}

	@Test
	public void anUnboundedLeafSortsToTheBackOfItsSegment() {
		// no bound estimable is transparent widening: sortable, last — not a partition
		Goal b3 = new FixedOrder(3), unbounded = new FixedOrder(Long.MAX_VALUE), b1 = new FixedOrder(1);
		Goal sorted = b3.and(unbounded).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b1, b3, unbounded);
	}

	@Test
	public void aSaturatedConjunctionSortsToTheBackOfItsSegment() {
		Goal huge = new FixedOrder(Long.MAX_VALUE / 2), b3 = new FixedOrder(3);
		Goal b2 = new FixedOrder(2), b1 = new FixedOrder(1);
		Goal sorted = b2.and(huge.and(b3)).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b1, b2, Conjunction.of(b3, huge));
	}

	@Test
	public void aConjunctionHoldingABarrierHoldsPosition() {
		Goal b3 = new FixedOrder(3), b1 = new FixedOrder(1), inner = new FixedOrder(1);
		Goal barrier = opaque();
		Goal sorted = b3.and(inner.and(barrier)).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b3, Conjunction.of(inner, barrier), b1);
	}

	@Test
	public void anExplicitBarrierHoldsPosition() {
		Goal b3 = new FixedOrder(3), b1 = new FixedOrder(1);
		Goal barrier = Barrier.of(new FixedOrder(1));
		Goal sorted = b3.and(barrier).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b3, barrier, b1);
	}

	@Test
	public void aPricedBarrierHoldsPositionOnlyWhileUnpriced() {
		Goal b5 = new FixedOrder(5), b1 = new FixedOrder(1);
		Goal unpriced = Barrier.priced(p -> Long.MAX_VALUE, new FixedOrder(1));
		Goal priced = Barrier.priced(p -> 2, new FixedOrder(1));

		Goal held = b5.and(unpriced).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) held).getClauses()).containsExactly(b5, unpriced, b1);

		Goal sorted = b5.and(priced).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses()).containsExactly(b1, priced, b5);
	}

	@Test
	public void aTabledCallHoldsPositionUntilItsEntryCompletesThenSortsByItsCount() {
		Tabled<Tuple1<Unifiable<Integer>>> rel = Tabling.define(t -> t.apply(x ->
				unify(x, lval(1)).or(unify(x, lval(2)))));
		Unifiable<Integer> out = lvar();
		Goal call = rel.apply(Tuple.of(out));
		Goal b5 = new FixedOrder(5), b1 = new FixedOrder(1);
		Goal conjunction = b5.and(call).and(b1);
		Knowledge p = Knowledge.empty().withStore(Table.empty());

		// the rewrite runs on its own scheduler, as Query's root rewrite does: pricing
		// a tabled call grounds a reify, which may not nest inside another ground
		// in progress: the entry is absent from the pricing package's table
		Goal held = conjunction.accept(new OrderingOptimizer().with(p));
		assertThat(((Conjunction) held).getClauses()).containsExactly(b5, call, b1);

		// completed: the full drain seals the entry, whose count (2) now prices the call
		assertThat(Query.of(call).from(p).on(TestSchedulers.factory()).solve(out).count()).isEqualTo(2);
		Goal sorted = conjunction.accept(new OrderingOptimizer().with(p));
		assertThat(((Conjunction) sorted).getClauses()).containsExactly(b1, call, b5);
	}

	@Test
	public void aDeferredBodySortsLastAndNeverPartitions() {
		// a deferred relation body is transparent widening: unknown order, movable
		Goal b3 = new FixedOrder(3), b1 = new FixedOrder(1);
		Goal deferred = Goal.defer(Goal::success);
		Goal sorted = b3.and(deferred).and(b1).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses()).containsExactly(b1, b3, deferred);
	}

	@Test
	public void aRecursiveRelationNoLongerHoldsItsSegment() {
		// R1 of docs/notes/conde-parks-until-enforce.md: appendo's recursive defer
		// made the whole relation a barrier, so the unification could not cross it
		Unifiable<LList<Integer>> x = lvar(), y = lvar();
		Unifiable<Integer> a = lvar();
		Goal appendo = Logic.appendo(x, y, LList.ofAll(1, 2, 3));
		Goal binding = unify(x, LList.of(a));

		Goal sorted = appendo.and(binding).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses()).containsExactly(binding, appendo);
	}

	/**
	 * R1's step counts under the fair driver, pinned exactly — a changed count is
	 * a decision. The optimizer walk is a plain recursion, no scheduler's work,
	 * so the counts are the search alone: as written, a split enumeration; by
	 * hand or by the pass, one unfolding once the unification runs first. The
	 * pass lands one step under the hand swap (it also normalizes the tree).
	 */
	@Test
	public void theOrderingPassReordersAppendo() {
		Unifiable<LList<Integer>> x = lvar(), y = lvar();
		Unifiable<Integer> a = lvar();
		Goal asWritten = Logic.appendo(x, y, LList.ofAll(1, 2, 3)).and(unify(x, LList.of(a)));
		Goal swapped = unify(x, LList.of(a)).and(Logic.appendo(x, y, LList.ofAll(1, 2, 3)));
		Optimizer ordering = Optimizer.pipeline(new CascadingOptimizer(), new OrderingOptimizer());

		assertThat(Query.of(asWritten).optimized(ordering).solve(y).count()).isEqualTo(1);
		assertThat(steps(Query.of(asWritten), y)).isEqualTo(199);
		assertThat(steps(Query.of(swapped), y)).isEqualTo(113);
		assertThat(steps(Query.of(asWritten).optimized(ordering), y)).isEqualTo(112);
	}

	private static <T> long steps(Query query, Unifiable<T> out) {
		AtomicLong count = new AtomicLong();
		StepListener counting = new StepListener() {
			@Override
			public void onStep(Fiber<?> node, Scope scope, String name) {
				count.incrementAndGet();
			}
		};
		query.on(fiber -> new BreadthFirstScheduler<>(fiber).withListener(counting)).solve(out)
				.collect(Collectors.toList());
		return count.get();
	}

	@Test
	public void ordersWithoutNormalizing() {
		// normalization is CascadingOptimizer's job, reached via the pipeline:
		// the ordering pass alone leaves nested disjunctions un-flattened
		Goal a = new FixedOrder(1), b = new FixedOrder(2), c = new FixedOrder(3);
		Goal nested = b.or(c);
		Goal rewritten = a.or(nested).accept(new OrderingOptimizer());
		assertThat(((Conde) rewritten).getClauses())
				.containsExactly(a, nested);
	}

	@Test
	public void derivesOrderThroughDisjunction() {
		Goal b2 = new FixedOrder(2), b3 = new FixedOrder(3), b4 = new FixedOrder(4);
		// conde(2, 3) has derived order 5 — the order-4 leaf sorts ahead of it
		Goal conde = b2.or(b3);
		Goal sorted = conde.and(b4).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b4, conde);
	}

	@Test
	public void aggregatesHoldPositionStructurally() {
		// an aggregate must never sort ahead of the goals that bind its inputs —
		// it is a barrier, not a cheap one-answer goal
		Goal agg = Aggregate.count(t -> Goal.success(), lvar());
		Goal b2 = new FixedOrder(2);
		Goal sorted = b2.and(agg).accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b2, agg);
	}

	@Test
	public void aggregatesAnswerDependsOnItsPosition() {
		// x ∈ {1,2}, THEN count the solutions of "x = 5": with x bound, zero.
		// Hoisting the count runs it under-bound — count 1 — which is a
		// DIFFERENT question, not a cheaper plan for the same one. The
		// optimizer's soundness theorem (more-bound ⇒ subset) does not cover
		// aggregates; holding position is what keeps this query meaning itself.
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> n = lvar();
		java.util.List<Integer> counts = Query.of(x.unifies(1).or(x.unifies(2))
				.and(Aggregate.count(t -> unify(x, lval(5)), n))).optimized(new OrderingOptimizer()).solve(n)
				.map(Term::get)
				.collect(Collectors.toList());

		assertThat(counts).containsExactly(0, 0);
	}

	@Test
	public void saturationClampsAndZeroAnnihilates() {
		assertThat(Semirings.SATURATING.times(Long.MAX_VALUE / 2, 3L)).isEqualTo(Long.MAX_VALUE);
		assertThat(Semirings.SATURATING.plus(Long.MAX_VALUE, 1L)).isEqualTo(Long.MAX_VALUE);
		assertThat(Semirings.SATURATING.times(0L, Long.MAX_VALUE)).isEqualTo(0);
	}

	@Test
	public void tabledCallsAreExplicitBarriers() {
		Goal tabled = Tabling.<Tuple1<Unifiable<Integer>>> define(t -> Goal.success())
				.apply(Tuple.of(lvar()));
		assertThat(tabled).isInstanceOf(Barrier.class);

		Goal b5 = new FixedOrder(5), b1 = new FixedOrder(1), b3 = new FixedOrder(3), b2 = new FixedOrder(2);
		Goal sorted = b5.and(b1).and(tabled).and(b3).and(b2)
				.accept(new OrderingOptimizer());
		assertThat(((Conjunction) sorted).getClauses())
				.containsExactly(b1, b5, tabled, b2, b3);
	}

	@Test
	public void ambientSolveYieldsTheSameAnswers() {
		Unifiable<Integer> x = lvar();
		Goal g = unify(x, lval(3)).or(unify(x, lval(4)));
		assertThat(Query.of(g).optimized(Optimizer.pipeline(new CascadingOptimizer(), new OrderingOptimizer())).solve(x)
				.map(Object::toString).collect(Collectors.toList()))
				.hasSameElementsAs(
						Query.of(g).on(TestSchedulers.factory()).solve(x).map(Object::toString).collect(Collectors.toList()));
	}

	@Test
	public void recursionUnfoldsThroughTheDeferHook() {
		Unifiable<Integer> x = lvar();
		assertThat(Query.of(countdown(x, 3)).optimized(new OrderingOptimizer()).solve(x)
				.map(Object::toString).collect(Collectors.toList()))
				.hasSameElementsAs(
						Query.of(countdown(x, 3)).on(TestSchedulers.factory()).solve(x).map(Object::toString).collect(Collectors.toList()));
	}

	private static Goal countdown(Unifiable<Integer> x, int n) {
		return n == 0 ?
				unify(x, lval(0)) :
				unify(x, lval(n)).or(Goal.defer(() -> countdown(x, n - 1)));
	}
}
