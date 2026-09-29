package org.clauseway.logic.unification;

// ABOUTME: Pins tuples as structural terms: members unify positionally through
// ABOUTME: the structural contract, one generic row for every arity.

import org.clauseway.logic.solving.Query;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.tuples.Tuple;

import org.clauseway.logic.TestSchedulers;
import java.util.List;
import java.util.stream.Collectors;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class TupleTermsTest {

	@Test
	public void tuplesUnifyMemberwise() {
		Unifiable<Long> x = lvar();
		Unifiable<Long> seven = lval(7L);
		List<Long> answers = Query.of(lval(Tuple.of(x, 1L)).unifies(lval(Tuple.of(seven, 1L)))).on(TestSchedulers.factory()).solve(x)
				.map(Term::get).collect(Collectors.toList());
		assertThat(answers).containsExactly(7L);
	}

	@Test
	public void aGroundMismatchRefuses() {
		Unifiable<Long> x = lvar();
		Unifiable<Long> seven = lval(7L);
		assertThat(Query.of(lval(Tuple.of(x, 1L)).unifies(lval(Tuple.of(seven, 2L)))).on(TestSchedulers.factory()).solve(x).count()).isZero();
	}

	@Test
	public void arityIsStructural() {
		Object one = Tuple.of(lval(1L));
		Object two = Tuple.of(lval(1L), lval(2L));
		assertThat(Query.of(lval(one).unifies(lval(two))).on(TestSchedulers.factory()).solve(lvar()).count()).isZero();
	}

	@Test
	public void tuplesBeyondArityEightUnifyThroughTheSameRow() {
		// the contract lives on the type: the flat TupleN needs no gate change
		Unifiable<Long> x = lvar();
		Unifiable<Long> nine = lval(9L);
		Object left = Tuple.ofAll(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, x);
		Object right = Tuple.ofAll(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, nine);
		List<Long> answers = Query.of(lval(left).unifies(lval(right))).on(TestSchedulers.factory()).solve(x)
				.map(Term::get).collect(Collectors.toList());
		assertThat(answers).containsExactly(9L);
	}

	@Test
	public void wideTuplesWalkAllTheWayDown() {
		Unifiable<Long> x = lvar();
		Unifiable<Long> y = lvar();
		Unifiable<Long> seven = lval(7L);
		Unifiable<Long> eight = lval(8L);
		List<Long> answers = Query.of(lval(Tuple.of(1L, 2L, 3L, 4L, 5L, 6L, (Object) x, (Object) y))
				.unifies(lval(Tuple.of(1L, 2L, 3L, 4L, 5L, 6L, (Object) seven, (Object) eight)))).on(TestSchedulers.factory()).solve(y)
				.map(Term::get).collect(Collectors.toList());
		assertThat(answers).containsExactly(8L);
	}
}
