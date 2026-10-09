package org.clauseway.logic.constraints.store;

// ABOUTME: A parked search effect, with a wake condition over the shared substitution
// ABOUTME: and a flush policy at the boundary: FAIL (an owed condition) or FORCE (run it).

import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.Substitutions;
import org.clauseway.logic.unification.terms.Term;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * {@code (watched, ripe, body)}: the driver re-examines the suspension when a
 * watched chain binds; when {@code ripe} holds, the body joins the run lane and
 * the suspension is gone — fired once, forever. A store may emit one via
 * {@code Revision.Updated.withSuspend}; the degenerate form (no watched terms, always
 * ripe) is an immediate run.
 *
 * <p><b>The ripeness contract.</b> {@code ripe} receives the {@link
 * Substitutions} view, so it is structurally scoped to shared knowledge: it
 * cannot see domains, records or any other store factor — factor-conditioned
 * reactions belong to the store that owns the factor. What the type cannot
 * enforce is MONOTONICITY, so here it is, literally: once {@code ripe} is true
 * in some state, it must remain true in every state derived from it —
 * equivalently, ADDING BINDINGS MUST NEVER FALSIFY THE CONDITION (the predicate
 * must be upward-closed in the substitution order). The driver only samples the
 * condition at statement time and when a watched chain binds; monotonicity is
 * exactly what makes that lazy sampling as good as watching continuously, and
 * what keeps firing independent of scheduler and agenda order. Rule of thumb:
 * conditions about the PRESENCE of knowledge qualify ("x is ground", "x and y
 * are both bound", "x == y is decided"); conditions about its ABSENCE do not
 * ("x is still unbound", "fewer than two are bound") — those are
 * negation-as-failure, whose home is committed choice, not the suspension lane.
 *
 * <p><b>The flush policy.</b> What a parked mechanism means at a boundary —
 * an answer leaving, a {@link org.clauseway.logic.goals.optimizer.Barrier}:
 * {@link Flush#FAIL}, an owed condition that may not ride an answer; or
 * {@link Flush#FORCE}, pending search that runs there. A {@link #forced}
 * suspension watches nothing and is never woken by the substrate — only the
 * boundary runs it (a parked disjunction, the same policy labelling has).
 */
public final class Suspension {

	/** End-of-branch treatment of a parked mechanism. */
	public enum Flush {
		FAIL, FORCE
	}

	private final Iterable<? extends Term<?>> watched;
	private final Predicate<Substitutions> ripe;
	private final Goal body;
	private final Flush flush;

	private Suspension(Iterable<? extends Term<?>> watched, Predicate<Substitutions> ripe, Goal body, Flush flush) {
		this.watched = watched;
		this.ripe = ripe;
		this.body = body;
		this.flush = flush;
	}

	/** Woken when a watched chain binds and {@code ripe} holds; an owed condition until then. */
	public static Suspension of(Iterable<? extends Term<?>> watched, Predicate<Substitutions> ripe, Goal body) {
		return new Suspension(watched, ripe, body, Flush.FAIL);
	}

	/** Never woken by the substrate: pending search, run at the next boundary. */
	public static Suspension forced(Goal body) {
		return new Suspension(Collections.emptyList(), s -> false, body, Flush.FORCE);
	}

	public Flush flush() {
		return flush;
	}

	public boolean isRipe(Knowledge state) {
		return ripe.test(state.substitution());
	}

	public boolean watchesAny(Knowledge state, Term<?> changed) {
		for (Term<?> w : watched) {
			if (Watches.matchesStructurally(state.substitution(), w, changed)) {
				return true;
			}
		}
		return false;
	}

	public Goal body() {
		return body;
	}

	@Override
	public String toString() {
		return flush == Flush.FORCE ? "parked(" + body + ")" : "suspend" + watched;
	}
}
