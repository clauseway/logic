package org.clauseway.logic.constraints.store;

// ABOUTME: The plan-space value: a family-homogeneous set of atoms with the
// ABOUTME: covering order — syntax of knowledge, no context, laws once.

import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.clauseway.functional.algebra.Absorbing;
import org.clauseway.functional.algebra.PartialOrder;
import org.clauseway.functional.algebra.Semilattice;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.unification.MiniKanren;
import org.clauseway.logic.unification.terms.LVar;
import org.clauseway.logic.unification.terms.Term;
import org.clauseway.vavr.Tuple;
import org.clauseway.vavr.Tuple2;
import org.clauseway.vavr.collection.LinkedHashMap;
import org.clauseway.vavr.collection.LinkedHashSet;
import org.clauseway.vavr.collection.Traversable;
import org.clauseway.vavr.control.Option;

/**
 * A factor's knowledge as SYNTAX: the set of atoms that state it, held as a
 * value. No context — bindings were baked into the atoms by whatever walk
 * produced them; applying new context is the execution plane's business
 * (absorb: fold {@code meet(Atom)} into a factor, then normalize). The
 * algebra is generic and lawful once: {@code meet} is insert/filter —
 * union, then slot-mates that declare the {@link Semilattice} capability
 * combine (same name, same watched surface: what an atom kind knows about
 * its own combining — {@code x⊂{1,2} ⊗ x⊂{2,3} = x⊂{2}}), then ENTAILMENT
 * dedup: an atom strictly dominated by another drops (subsumption
 * deletion: stating ¬(A ∧ B) alongside ¬A adds nothing). {@code leq} is
 * the COVERING order — every atom of the wider theory
 * entailed by some atom of this one — grade two of the leq tower
 * (structural ⊂ covering ⊂ the factor's semantic leq): sound always,
 * sharp exactly as far as the atom classes' own {@code leq} overrides
 * reach, blind to conjunctive entailment by construction. The dedup is
 * what makes the two agree: on plain sets the covering order neither
 * reverses accumulation nor stays antisymmetric ({¬A} and {¬A, ¬(A∧B)}
 * cover each other); on subsumption-free sets both laws hold, as far as
 * the atom leq is itself antisymmetric mod equals — the sharp overrides'
 * obligation.
 *
 * <p>Family homogeneity is the type parameter's promise alone — atoms are
 * typed over their factor, so a theory of {@code F} can only be handed
 * {@code F}'s atoms; there is no runtime door.
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Theory<F extends Factor<F>> implements Semilattice<Theory<F>>, PartialOrder<Theory<F>>, Absorbing {

	private final LinkedHashSet<Atom<F>> atoms;

	/**
	 * The slot index, derived from {@code atoms} (excluded from equality):
	 * collision key → THE atom on that slot, one hop. Single occupancy is
	 * the atom kinds' obligation — a kind whose knowledge accumulates on one
	 * surface holds it as a collection and declares {@link Semilattice}.
	 * The key is the collection the atom already holds, so its equality is
	 * the kind's identity granularity and keying costs nothing.
	 */
	private final LinkedHashMap<Slot, Atom<F>> slots;

	/**
	 * The by-kind index, derived like {@code slots} (excluded from equality):
	 * concrete atom class → its atoms, so a factor's kind-specific reads
	 * (impositions, propagators, residents) iterate exactly their kind
	 * instead of filtering the whole bag.
	 */
	private final LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kinds;

	/**
	 * The ⊥ flag, kept as atoms arrive and leave (excluded from equality — equality
	 * is the atoms): an atom declaring the {@link Absorbing} capability and
	 * answering true makes the whole theory refutational. A legal plan
	 * value; only execution reads it as failure.
	 */
	private final boolean absorbing;

	/**
	 * The collision key: same family, same name, same held watched
	 * collection = same slot. The family rides in the key because atom
	 * names are unique only WITHIN a family (every lattice family names
	 * its value atoms "imposition").
	 */
	@Value
	private static class Slot {
		Class<?> family;
		String name;
		Traversable<Term<?>> surface;
	}

	private static Slot slotOf(Atom<?> atom) {
		return new Slot(atom.getFactorClass(), atom.name(), atom.watched());
	}

	public static <F extends Factor<F>> Theory<F> empty() {
		return new Theory<>(LinkedHashSet.empty(), LinkedHashMap.empty(), LinkedHashMap.empty(),
				false);
	}

	@Override
	public boolean isAbsorbing() {
		return absorbing;
	}

	private static boolean absorbs(Atom<?> atom) {
		return atom instanceof Absorbing && ((Absorbing) atom).isAbsorbing();
	}

	public static <F extends Factor<F>> Theory<F> of(Iterable<? extends Atom<F>> atoms) {
		Theory<F> theory = empty();
		for (Atom<F> atom : atoms) {
			theory = theory.meetSingle(atom)._1;
		}
		return theory.withoutDominated(theory.atoms);
	}

	/**
	 * The capability meet at one slot: an atom kind that declares
	 * {@link Semilattice} knows how to meet its own slot-mates — combining
	 * is family knowledge, read here as a capability, never assumed. A kind
	 * that cannot combine yet collides has broken the single-occupancy
	 * obligation: hold the collection inside the atom. Answers the theory
	 * with the atom in residence and the atom that moved — the arrival, or
	 * the combination standing in the occupant's position — none when the
	 * occupant already carried it.
	 */
	private Tuple2<Theory<F>, Option<Atom<F>>> meetSingle(Atom<F> atom) {
		Slot slot = slotOf(atom);
		Option<Atom<F>> occupant = slots.get(slot);
		if (!occupant.isDefined()) {
			return Tuple.of(new Theory<>(
					atoms.add(atom),
					slots.put(slot, atom),
					kindAdded(kinds, atom),
					absorbing || absorbs(atom)), Option.some(atom));
		}
		Atom<F> prior = occupant.get();
		if (prior.equals(atom)) {
			return Tuple.of(this, Option.none());
		}
		if (prior instanceof Semilattice && atom instanceof Semilattice) {
			Atom<F> combined = combine(prior, atom);
			if (combined.equals(prior)) {
				return Tuple.of(this, Option.none());
			}
			return Tuple.of(new Theory<>(
					atoms.replace(prior, combined),
					slots.put(slot, combined),
					kindReplaced(kinds, prior, combined),
					absorbing || absorbs(combined)), Option.some(combined));
		}
		throw uncombinable(prior, atom);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <F extends Factor<F>> Atom<F> combine(Atom<F> a, Atom<F> b) {
		return (Atom<F>) ((Semilattice) a).combine((Semilattice) b);
	}

	private static IllegalStateException uncombinable(Atom<?> prior, Atom<?> atom) {
		return new IllegalStateException(
				"slot-mates that cannot combine — the kind must hold its collection: "
						+ prior + " vs " + atom);
	}

	/**
	 * Subsumption deletion among the suspects: a resident STRICTLY dominated
	 * by a suspect drops, and so does a suspect dominated by anyone
	 * (mutually-entailing distinct atoms both stay — dropping either would
	 * lose knowledge the other's structure does not carry). Residents never
	 * meet residents: a theory in normal form is already subsumption-free, so only
	 * what moved can dominate or be dominated; with every atom a suspect
	 * this is the full sweep.
	 */
	private Theory<F> withoutDominated(LinkedHashSet<Atom<F>> suspects) {
		LinkedHashSet<Atom<F>> killed = atoms.filter(x -> suspects.contains(x) ?
				atoms.exists(y -> dominates(y, x)) :
				suspects.exists(s -> dominates(s, x)));
		return killed.isEmpty() ? this : without(killed);
	}

	/** The removal bookkeeping: atoms, slot index, kind index, ⊥ flag. */
	private Theory<F> without(LinkedHashSet<Atom<F>> killed) {
		LinkedHashSet<Atom<F>> kept = atoms.removeAll(killed);
		LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> keptKinds = kinds;
		for (Atom<F> atom : killed) {
			keptKinds = kindRemoved(keptKinds, atom);
		}
		return new Theory<>(kept,
				slots.filterValues(kept::contains),
				keptKinds,
				absorbing && kept.exists(Theory::absorbs));
	}

	/** Strict domination: {@code b} entails {@code a} and not the reverse. */
	private static <F extends Factor<F>> boolean dominates(Atom<F> b, Atom<F> a) {
		return !b.equals(a) && b.leq(a) && !a.leq(b);
	}

	public LinkedHashSet<Atom<F>> atoms() {
		return atoms;
	}

	/** The slot occupant, one hop — the factor's by-surface read. */
	public Option<Atom<F>> atom(Class<?> family, String name, Traversable<Term<?>> surface) {
		return slots.get(new Slot(family, name, surface));
	}

	/**
	 * One kind's atoms, streamed — the factor's kind-specific iteration.
	 * Buckets are keyed by CONCRETE class and matched by assignability, so
	 * an abstract kind (Propagator's schema subclasses) streams all its
	 * implementors; the scan is over the handful of distinct classes, not
	 * the atoms.
	 */
	public <K> java.util.stream.Stream<K> kind(Class<K> kind) {
		return kinds.toJavaStream()
				.filter(entry -> kind.isAssignableFrom(entry._1))
				.flatMap(entry -> entry._2.toJavaStream())
				.map(kind::cast);
	}

	/**
	 * The incremental door: insert one atom, combining at its slot — NO
	 * domination sweep. Agrees with {@link #meet} exactly when the kinds'
	 * leq is slot-local (impositions, propagators — the lattice family);
	 * a family with cross-slot entailment (nogood subsumption) must meet.
	 */
	public Theory<F> with(Atom<F> atom) {
		return meetSingle(atom)._1;
	}

	private static <F extends Factor<F>> LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kindAdded(
			LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kinds, Atom<F> atom) {
		return kinds.put(atom.getClass(), kinds.get(atom.getClass())
				.getOrElse(LinkedHashSet.empty())
				.add(atom));
	}

	private static <F extends Factor<F>> LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kindRemoved(
			LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kinds, Atom<F> atom) {
		LinkedHashSet<Atom<F>> bucket = kinds.get(atom.getClass())
				.getOrElse(LinkedHashSet.empty())
				.remove(atom);
		return bucket.isEmpty() ?
				kinds.remove(atom.getClass()) :
				kinds.put(atom.getClass(), bucket);
	}

	private static <F extends Factor<F>> LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kindReplaced(
			LinkedHashMap<Class<?>, LinkedHashSet<Atom<F>>> kinds, Atom<F> prior, Atom<F> combined) {
		if (!prior.getClass().equals(combined.getClass())) {
			return kindAdded(kindRemoved(kinds, prior), combined);
		}
		return kinds.put(prior.getClass(), kinds.get(prior.getClass())
				.getOrElse(LinkedHashSet.empty())
				.replace(prior, combined));
	}

	/**
	 * Occupant removal — NOT an algebra operation (knowledge only grows
	 * under ⊗): the execution plane's own-factor surgery, for a store
	 * discharging spent entries and subsumed propagators. A non-occupant
	 * leaves the theory untouched.
	 */
	public Theory<F> without(Atom<F> atom) {
		return slots.get(slotOf(atom))
				.filter(atom::equals)
				.map(occupant -> without(LinkedHashSet.of(occupant)))
				.getOrElse(this);
	}

	public boolean isEmpty() {
		return atoms.isEmpty();
	}

	/**
	 * ⊗: insert/filter — union, combine slot-mates, delete dominated. An index
	 * merge: both sides' surfaces are already collected, so the combining phase
	 * is linear in the right-hand theory.
	 *
	 * <p>IDENTITY-PRESERVING: when nothing moves — every incoming atom equal
	 * to its occupant or combining back to it — the receiver returns ITSELF, so
	 * a door's no-op guard is reference equality. (One corner rebuilds an
	 * equal theory: an incoming atom the domination filter kills; the door
	 * then queues a no-op the family answers cheaply.)
	 *
	 * <p>The domination phase is incremental too: only the atoms that moved
	 * are suspects, so the cull is linear in the receiver per arrival.
	 */
	public Theory<F> meet(Theory<F> other) {
		Theory<F> theory = this;
		LinkedHashSet<Atom<F>> moved = LinkedHashSet.empty();
		for (Atom<F> atom : other.atoms) {
			Tuple2<Theory<F>, Option<Atom<F>>> step = theory.meetSingle(atom);
			theory = step._1;
			if (step._2.isDefined()) {
				moved = moved.add(step._2.get());
			}
		}
		return moved.isEmpty() ? this : theory.withoutDominated(moved);
	}

	@Override
	public Theory<F> combine(Theory<F> other) {
		return meet(other);
	}

	/**
	 * ⊥ absorbs: accumulation against a refuted branch contributes nothing —
	 * the guard the factor order carried before residence moved. The live
	 * half IS the covering order read backwards: meeting me into {@code
	 * other} adds nothing iff other already entails every atom of mine —
	 * allocation-free, early-exit, never a scratch meet.
	 */
	@Override
	public boolean absorbedBy(Theory<F> other) {
		return other.absorbing || other.leq(this);
	}

	/**
	 * The covering order: this ⊑ other iff every atom of {@code other} is
	 * entailed by SOME atom of this — delegates to {@link Atom#leq}, so its
	 * sharpness is exactly what the atom classes invest.
	 */
	@Override
	public boolean leq(Theory<F> other) {
		return other.atoms.forAll(b -> atoms.exists(a -> a.leq(b)));
	}

	/**
	 * The name cut: an atom is covered iff every name its watched surface
	 * touches is supplied (grounds are always covered); coupled atoms go to
	 * the remainder. {@code _1 ⊗ _2 = this}.
	 */
	public Tuple2<Theory<F>, Theory<F>> split(List<LVar<?>> vars) {
		LinkedHashSet<Atom<F>> covered = LinkedHashSet.empty();
		LinkedHashSet<Atom<F>> remainder = LinkedHashSet.empty();
		for (Atom<F> atom : atoms) {
			boolean in = atom.watched().forAll(term ->
					MiniKanren.namesIn(term).allMatch(vars::contains));
			if (in) {
				covered = covered.add(atom);
			} else {
				remainder = remainder.add(atom);
			}
		}
		return Tuple.of(of(covered), of(remainder));
	}

	/**
	 * The crossing, atom by atom. Re-normalizes: a renaming that merges names
	 * can create slot collisions and dominations the source theory did not
	 * have.
	 */
	public Fiber<Theory<F>> rename(Renaming renaming) {
		Fiber<LinkedHashSet<Atom<F>>> renamed = Fiber.done(LinkedHashSet.empty());
		for (Atom<F> atom : atoms) {
			renamed = renamed.flatMap(acc ->
					atom.rename(renaming).map(acc::add));
		}
		return renamed.map(Theory::of);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Theory)) {
			return false;
		}
		Theory<?> other = (Theory<?>) o;
		return atoms.equals(other.atoms);
	}

	@Override
	public int hashCode() {
		return Objects.hash(atoms);
	}

	@Override
	public String toString() {
		return atoms.mkString("{", " ⊗ ", "}");
	}
}
