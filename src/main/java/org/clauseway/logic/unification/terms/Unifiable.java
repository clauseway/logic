package org.clauseway.logic.unification.terms;

import static org.clauseway.logic.unification.terms.LVal.lval;

import org.clauseway.logic.constraints.Constraints;
import org.clauseway.logic.constraints.Posting;

/**
 * A term that may enter a solver: goals are built by unifying these.
 *
 * @author TGa
 */
public interface Unifiable<T> extends Term<T> {

	/**
	 * The two terms made equal, with the occurs check: a binding that would
	 * put a variable inside its own value is refused and the goal fails. The
	 * engine's terms are finite trees, so {@code tail = (head . tail)} has no
	 * solution; failing is the answer, and a branch that asks it is pruned
	 * like any other dead branch.
	 */
	default Posting unifies(Unifiable<T> rhs) {
		return Constraints.unify(this, rhs);
	}

	default Posting unifies(T value) {
		return Constraints.unify(this, lval(value));
	}

	/**
	 * The same, WITHOUT the occurs check — a speed door, used where one side
	 * is an atom or is otherwise known not to contain the other. The caller
	 * promises no cycle can close. If the promise is broken the cycle is built
	 * silently and the next reification of it never terminates: the walk
	 * does not record what it has visited, by design, because that would cost
	 * every reification what this door saves. Prefer {@link #unifies}.
	 */
	default Posting unifiesNc(Unifiable<T> rhs) {
		return Constraints.unifyNc(this, rhs)
				.named("unifyNc");
	}

	default Posting unifiesNc(T value) {
		return unifiesNc(lval(value));
	}

	@SuppressWarnings("unchecked")
	@Override
	default Unifiable<T> unifiable() {
		return this;
	}

	default Unifiable<Object> getObjectUnifiable() {
		return (Unifiable<Object>) this;
	}
}
