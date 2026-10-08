package org.clauseway.logic.constraints;

// ABOUTME: A store that owes search before its branch may be judged — settled by
// ABOUTME: Propagation.settle at every barrier and wherever an answer leaves.

import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Packaged;

/**
 * The settle trigger, for stores that hold pending SEARCH: a package citizen
 * whose presence registers it, so that {@link Propagation#settle} runs what it
 * owes before a {@link org.clauseway.logic.goals.optimizer.Barrier} runs or an
 * answer leaves. {@link #settle()} is a goal because settling may fork; it
 * runs again on each child until no citizen is {@link #pending()}.
 *
 * <p>Not a {@link org.clauseway.logic.constraints.store.Factor}: a factor's
 * branching is knowledge — a theory that propagates, keys a tabled call and
 * rides an answer as a condition — judged at propagation strength everywhere
 * and committed only at reification ({@code Factor.enforce}). A pending
 * citizen is code: nothing is known about it until it runs, so it runs before
 * anything judges the branch. Distinct too from a parked suspension, an owed
 * CONDITION, which may ride through a judge and is refused only where an
 * answer leaves.
 */
public interface Pending extends Packaged {

	/** True while this store still owes search. */
	boolean pending();

	/** Runs one step of what is owed; may fork, may leave more pending. */
	Goal settle();
}
