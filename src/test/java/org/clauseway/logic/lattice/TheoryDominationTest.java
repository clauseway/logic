package org.clauseway.logic.lattice;

// ABOUTME: Subsumption deletion under meet: what domination drops, and that
// ABOUTME: digesting arrivals costs leq calls linear in the resident theory.

import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.constraints.store.Atom;
import org.clauseway.logic.constraints.store.Renaming;
import org.clauseway.logic.constraints.store.Theory;
import org.clauseway.logic.lattice.LatticeFactorTest.FlatConstraints;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.vavr.collection.HashSet;
import org.clauseway.vavr.collection.Traversable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.Before;
import org.junit.Test;

/**
 * A nogood-shaped test atom: {@code this ⊑ other} iff this atom's literals
 * are a subset of the other's (the shorter nogood entails the longer), with
 * every {@code leq} call counted. Each atom is its own slot, so meet never
 * combines and the only normalization left is domination.
 */
public class TheoryDominationTest {

	private static final AtomicLong LEQ_CALLS = new AtomicLong();

	@EqualsAndHashCode(of = "literals")
	@ToString(of = "literals")
	static final class Forbids implements Atom<FlatConstraints> {
		private final HashSet<String> literals;

		Forbids(String... literals) {
			this.literals = HashSet.of(literals);
		}

		@Override
		public boolean leq(Atom<FlatConstraints> other) {
			LEQ_CALLS.incrementAndGet();
			return other instanceof Forbids && literals.forAll(((Forbids) other).literals::contains);
		}

		@Override
		public Class<? extends FlatConstraints> getFactorClass() {
			return FlatConstraints.class;
		}

		@Override
		public String name() {
			return literals.mkString("¬(", "∧", ")");
		}

		@Override
		public Traversable<Term<?>> watched() {
			return HashSet.empty();
		}

		@Override
		public Fiber<Atom<FlatConstraints>> rename(Renaming renaming) {
			return Fiber.done(this);
		}

		@Override
		public FlatConstraints empty() {
			return FlatConstraints.empty();
		}
	}

	private static Theory<FlatConstraints> unit(Atom<FlatConstraints> atom) {
		return Theory.of(Collections.singletonList(atom));
	}

	@Before
	public void resetCounter() {
		LEQ_CALLS.set(0);
	}

	@Test
	public void anArrivalDominatingAResidentDropsTheResident() {
		Theory<FlatConstraints> resident = Theory.of(java.util.Arrays.<Atom<FlatConstraints>> asList(
				new Forbids("a"), new Forbids("b", "c")));

		assertThat(resident.meet(unit(new Forbids("b"))).atoms())
				.containsExactlyInAnyOrder(new Forbids("a"), new Forbids("b"));
	}

	@Test
	public void anArrivalDominatedByAResidentIsDropped() {
		Theory<FlatConstraints> resident = Theory.of(java.util.Arrays.<Atom<FlatConstraints>> asList(
				new Forbids("a"), new Forbids("b", "c")));

		assertThat(resident.meet(unit(new Forbids("a", "x"))).atoms())
				.containsExactlyInAnyOrder(new Forbids("a"), new Forbids("b", "c"));
	}

	@Test
	public void arrivalsDominateEachOther() {
		Theory<FlatConstraints> resident = unit(new Forbids("z"));
		Theory<FlatConstraints> arriving = Theory.of(java.util.Arrays.<Atom<FlatConstraints>> asList(
				new Forbids("a", "b"), new Forbids("a")));

		assertThat(resident.meet(arriving).atoms())
				.containsExactlyInAnyOrder(new Forbids("z"), new Forbids("a"));
	}

	@Test
	public void aDominatingArrivalThatAlsoKillsKeepsTheIndexesInStep() {
		Theory<FlatConstraints> resident = Theory.of(java.util.Arrays.<Atom<FlatConstraints>> asList(
				new Forbids("a"), new Forbids("b", "c")));
		Theory<FlatConstraints> met = resident.meet(unit(new Forbids("b")));

		assertThat(met.atom(FlatConstraints.class, new Forbids("b", "c").name(), HashSet.empty())).isEmpty();
		assertThat(met.atom(FlatConstraints.class, new Forbids("b").name(), HashSet.empty()))
				.contains(new Forbids("b"));
		assertThat(met.kind(Forbids.class)).containsExactlyInAnyOrder(new Forbids("a"), new Forbids("b"));
		assertThat(met).isEqualTo(Theory.of(java.util.Arrays.<Atom<FlatConstraints>> asList(
				new Forbids("a"), new Forbids("b"))));
	}

	@Test
	public void meetingOneAtomIntoATheoryOfNCostsLeqCallsLinearInN() {
		int n = 64;
		List<Atom<FlatConstraints>> atoms = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			atoms.add(new Forbids("l" + i));
		}
		Theory<FlatConstraints> resident = Theory.of(atoms);
		LEQ_CALLS.set(0);

		Theory<FlatConstraints> met = resident.meet(unit(new Forbids("fresh")));

		assertThat(met.atoms()).hasSize(n + 1);
		// one arrival against n residents, each direction at most once
		assertThat(LEQ_CALLS.get()).isLessThanOrEqualTo(2L * n);
	}
}
