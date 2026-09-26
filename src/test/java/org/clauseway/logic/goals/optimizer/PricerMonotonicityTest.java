package org.clauseway.logic.goals.optimizer;

// ABOUTME: Law-tests the shipped pricers along growing-knowledge chains: a price
// ABOUTME: must never rise as knowledge grows — stale prices stay sound.

import static org.clauseway.logic.constraints.Constraints.unify;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.algebra.laws.MonotoneLaws;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.logic.finitedomain.FiniteDomain;
import org.clauseway.logic.finitedomain.FiniteDomainTestSupport;
import org.clauseway.logic.finitedomain.Longs;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.tabling.table.Table;
import org.clauseway.logic.tabling.Tabled;
import org.clauseway.logic.tabling.Tabling;
import org.clauseway.logic.unification.terms.Unifiable;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.functional.tuples.Tuple1;
import java.util.Arrays;
import java.util.function.BiPredicate;
import org.junit.Test;

public class PricerMonotonicityTest {

	/** counts ordered downward: more knowledge in, fewer-or-equal answers out */
	private static final BiPredicate<Long, Long> NEVER_RISES = (lo, hi) -> lo >= hi;

	@Test
	public void domPostPriceNeverRises() {
		Unifiable<Long> x = lvar();
		Goal post = FiniteDomain.dom(x, Longs.interval(8, 12));
		Knowledge blind = Knowledge.empty();
		Knowledge overlapping = FiniteDomainTestSupport.withDomain(x, Longs.interval(0, 10));
		Knowledge disjoint = FiniteDomainTestSupport.withDomain(x, Longs.interval(0, 4));
		MonotoneLaws.check(
				Arrays.asList(blind, overlapping, disjoint),
				p -> ((Bounded) post).answers(p),
				NEVER_RISES);
	}

	@Test
	public void tabledCallPriceNeverRises() {
		Tabled<Tuple1<Unifiable<Integer>>> rel = Tabling.define(t -> t.apply(x ->
				unify(x, lval(1)).or(unify(x, lval(2)))));
		Unifiable<Integer> out = lvar();
		Goal call = rel.apply(Tuple.of(out));

		Knowledge noTable = Knowledge.empty();
		Knowledge incomplete = Knowledge.empty().withStore(Table.empty());
		Knowledge complete = Knowledge.empty().withStore(Table.empty());
		assertThat(call.solveFrom(complete, out, BreadthFirstScheduler::new).count()).isEqualTo(2);

		MonotoneLaws.check(
				Arrays.asList(noTable, incomplete, complete),
				p -> ((Bounded) call).answers(p),
				NEVER_RISES);
	}
}
