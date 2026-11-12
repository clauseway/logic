package org.clauseway.logic.unification;

// ABOUTME: A user's compound value signs the structural contract and becomes a term: unified
// ABOUTME: member-wise with its own kind only, walked, reified with its members bound, matched by one arm.

import static org.assertj.core.api.Assertions.assertThat;
import static org.clauseway.logic.goals.Matche.matche;
import static org.clauseway.logic.goals.Goal.defer;
import static org.clauseway.logic.goals.Matche.structure;
import static org.clauseway.logic.unification.structures.LOption.none;
import static org.clauseway.logic.unification.structures.LOption.some;
import static org.clauseway.logic.unification.terms.LVal.lval;
import static org.clauseway.logic.unification.terms.LVar.lvar;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.functional.tuples.Tuple2;
import org.clauseway.logic.finitedomain.Ints;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.solving.Query;
import org.clauseway.logic.unification.structures.LOption;
import org.clauseway.logic.unification.structures.Structural;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class StructuralTest {

	/** A point: two members, signed. */
	@Value
	static class Point implements Structural<Point> {
		Term<Integer> x;
		Term<Integer> y;

		@Override
		public Iterable<? extends Term<?>> members() {
			return Arrays.asList(x, y);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Point withMembers(Term<?>[] members) {
			return new Point((Term<Integer>) members[0], (Term<Integer>) members[1]);
		}
	}

	/** Another two-member kind, so arity alone never unifies. */
	@Value
	static class Span implements Structural<Span> {
		Term<Integer> from;
		Term<Integer> to;

		@Override
		public Iterable<? extends Term<?>> members() {
			return Arrays.asList(from, to);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Span withMembers(Term<?>[] members) {
			return new Span((Term<Integer>) members[0], (Term<Integer>) members[1]);
		}
	}

	private static Unifiable<Point> point(Term<Integer> x, Term<Integer> y) {
		return lval(new Point(x, y));
	}

	private static <T> boolean unifies(Unifiable<T> l, Unifiable<T> r) {
		return MiniKanren.unify(Substitutions.empty(), l, r).ground().isPresent();
	}

	@Test
	public void twoValuesOfOneKindUnifyMemberWise() {
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		List<Integer> answers = Query.of(point(x, lval(2)).unifies(point(lval(1), y))).solve(x)
				.map(Reified::get)
				.collect(Collectors.toList());
		assertThat(answers).containsExactly(1);
		assertThat(Query.of(point(x, lval(2)).unifies(point(lval(1), y))).solve(y).map(Reified::get)
				.collect(Collectors.toList())).containsExactly(2);
		assertThat(unifies(point(lval(1), lval(2)), point(lval(1), lval(3)))).isFalse();
	}

	@Test
	public void theKindIsTheType_equalArityNeverUnifiesAcrossKinds() {
		assertThat(unifies(
				point(lval(1), lval(2)).getObjectUnifiable(),
				lval(new Span(lval(1), lval(2))).getObjectUnifiable()))
				.isFalse();
		assertThat(unifies(
				point(lval(1), lval(2)).getObjectUnifiable(),
				lval(Tuple.of(lval(1), lval(2))).getObjectUnifiable()))
				.isFalse();
	}

	@Test
	public void aValueReifiesWithItsMembersBound() {
		Unifiable<Point> p = lvar();
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();
		Reified<Point> answer = Query.of(p.unifies(point(x, y)).and(x.unifies(1)).and(y.unifies(2)))
				.solve(p).findFirst().get();
		assertThat(answer.isGround()).isTrue();
		assertThat(answer.get().getX().get()).isEqualTo(1);
		assertThat(answer.get().getY().get()).isEqualTo(2);
	}

	@Test
	public void aValueWithAFreeMemberReifiesAsAHole() {
		Unifiable<Point> p = lvar();
		Reified<Point> answer = Query.of(p.unifies(point(lval(1), lvar()))).solve(p).findFirst().get();
		assertThat(answer.isGround()).isFalse();
		assertThat(answer.get().getX().get()).isEqualTo(1);
		assertThat(answer.get().getY().asReified()).isPresent();
	}

	@Test
	public void freshMintsTheKindWithEveryMemberAVariable() {
		Point fresh = new Point(lval(1), lval(2)).fresh();
		assertThat(fresh.getX().asVar()).isPresent();
		assertThat(fresh.getY().asVar()).isPresent();
		assertThat(fresh.getX()).isNotSameAs(fresh.getY());
	}

	@Test
	public void matcheTakesAStructureArm() {
		Unifiable<Point> p = lvar();
		Unifiable<Integer> sum = lvar();
		List<Integer> answers = Query.of(p.unifies(point(lval(3), lval(4)))
				.and(matche(p, structure(new Point(lval(0), lval(0)).fresh(), q -> added(q, sum)))))
				.solve(sum)
				.map(Reified::get)
				.collect(Collectors.toList());
		assertThat(answers).containsExactly(7);
	}

	@Test
	public void aSignedValueNestsInATupleAndATupleInASignedValue() {
		Unifiable<Integer> x = lvar();
		Unifiable<Tuple2<Unifiable<Point>, Unifiable<String>>> pair = lval(Tuple.of(point(x, lval(2)), lval("p")));
		List<Integer> answers = Query.of(pair.unifies(lval(Tuple.of(point(lval(1), lval(2)), lval("p"))))).solve(x)
				.map(Reified::get)
				.collect(Collectors.toList());
		assertThat(answers).containsExactly(1);

		Unifiable<Point> inner = lvar();
		Unifiable<Segment> s = lvar();
		Reified<Segment> answer = Query.of(s.unifies(lval(new Segment(inner, lval("s"))))
				.and(inner.unifies(point(lval(3), lval(4)))))
				.solve(s).findFirst().get();
		assertThat(answer.isGround()).isTrue();
		assertThat(answer.get().getStart().get().getX().get()).isEqualTo(3);
	}

	/** A pattern with a value among its members matches only what carries that value. */
	@Test
	public void thePatternParticipates_aValueMemberMustMatch() {
		Unifiable<Point> p = lvar();
		Unifiable<Integer> seen = lvar();
		Goal onTheAxis = matche(p, structure(new Point(lvar(), lval(0)), q -> seen.unifies((Unifiable<Integer>) q.getX())));
		assertThat(Query.of(p.unifies(point(lval(5), lval(0))).and(onTheAxis)).solve(seen)
				.map(Reified::get).collect(Collectors.toList())).containsExactly(5);
		assertThat(Query.of(p.unifies(point(lval(5), lval(1))).and(onTheAxis)).solve(seen).count()).isZero();
	}

	/** A signed value with a signed member. */
	@Value
	static class Segment implements Structural<Segment> {
		Term<Point> start;
		Term<String> label;

		@Override
		public Iterable<? extends Term<?>> members() {
			return Arrays.asList(start, label);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Segment withMembers(Term<?>[] members) {
			return new Segment((Term<Point>) members[0], (Term<String>) members[1]);
		}
	}

	/** The arm's body: the point's members added, once the arm has bound them. */
	private static Goal added(Point q, Unifiable<Integer> sum) {
		return Ints.addo((Unifiable<Integer>) q.getX(), (Unifiable<Integer>) q.getY(), sum);
	}
	// -- a kind that contains its own kind ----------------------------------

	/** A list as its own kind: a head and a tail that is none at the end, some(cons) otherwise. */
	@Value
	static class Cons implements Structural<Cons> {
		Term<Integer> head;
		Term<LOption<Cons>> tail;

		@Override
		public Iterable<? extends Term<?>> members() {
			return Arrays.asList(head, tail);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Cons withMembers(Term<?>[] members) {
			return new Cons((Term<Integer>) members[0], (Term<LOption<Cons>>) members[1]);
		}
	}

	private static Unifiable<Cons> cons(Term<Integer> head, Term<LOption<Cons>> tail) {
		return lval(new Cons(head, tail));
	}

	private static Unifiable<Cons> list(Term<Integer> last) {
		return cons(lval(1), some(cons(lval(2), some(cons(last, none())))));
	}

	/** The sum of the list: the head plus the sum of the tail, a recursion through the kind's own member. */
	private static Goal sumo(Unifiable<Cons> list, Unifiable<Integer> total) {
		return matche(list, structure(new Cons(lvar(), lvar()), c -> {
			Unifiable<Integer> head = (Unifiable<Integer>) c.getHead();
			Unifiable<LOption<Cons>> tail = (Unifiable<LOption<Cons>>) c.getTail();
			return tail.unifies(none()).and(total.unifies(head))
					.or(defer(() -> {
						Unifiable<Cons> rest = lvar();
						Unifiable<Integer> sub = lvar();
						return tail.unifies(some(rest)).and(sumo(rest, sub)).and(Ints.addo(head, sub, total));
					}));
		}));
	}

	@Test
	public void aValueOfItsOwnKindUnifiesThroughItsDepth() {
		Unifiable<Integer> x = lvar();
		List<Integer> answers = Query.of(list(x).unifies(list(lval(3)))).solve(x)
				.map(Reified::get)
				.collect(Collectors.toList());
		assertThat(answers).containsExactly(3);
		assertThat(unifies(list(lval(3)), cons(lval(1), some(cons(lval(2), none()))))).isFalse();
	}

	@Test
	public void aValueOfItsOwnKindReifiesToItsDepth() {
		Unifiable<Cons> whole = lvar();
		Unifiable<Integer> x = lvar();
		Reified<Cons> answer = Query.of(whole.unifies(list(x)).and(x.unifies(3))).solve(whole).findFirst().get();
		assertThat(answer.isGround()).isTrue();
		Cons third = answer.get().getTail().get().getValue().get().getTail().get().getValue().get();
		assertThat(third.getHead().get()).isEqualTo(3);
		assertThat(third.getTail().get().isNone()).isTrue();
	}

	/** A binding that would put a value inside itself through a signed member is refused, not built. */
	@Test
	public void theOccursCheckSeesThroughASignedMember() {
		Unifiable<Cons> c = lvar();
		assertThat(Query.of(c.unifies(cons(lval(1), some(c)))).solve(c).count()).isZero();
	}

	@Test
	public void aRecursiveRuleWalksTheKindWithStructureArms() {
		Unifiable<Integer> total = lvar();
		List<Integer> answers = Query.of(sumo(list(lval(3)), total)).solve(total)
				.map(Reified::get)
				.collect(Collectors.toList());
		assertThat(answers).containsExactly(6);
	}
	/** A member unified with the value that holds it: the occurs check refuses the cycle at the binding. */
	@Test
	public void aMemberDoesNotUnifyWithTheValueThatHoldsIt() {
		Unifiable<Integer> head = lvar();
		Unifiable<LOption<Cons>> tail = lvar();
		Unifiable<Cons> lst = cons(head, tail);
		assertThat(Query.of(tail.unifies(some(lst))).solve(lst).count()).isZero();
	}

}
