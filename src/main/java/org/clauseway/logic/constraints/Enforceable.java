package org.clauseway.logic.constraints;

// ABOUTME: A store that owes work before its branch may be judged finite —
// ABOUTME: settled by Propagation.enforce at every exit and committed-choice judge.

import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Packaged;

/**
 * The enforce trigger, for stores that hold pending SEARCH: a package citizen
 * whose presence registers it, so that {@link Propagation#enforce} settles what
 * it owes before an answer leaves or a committed choice judges the branch.
 * {@link #enforce()} is a goal because settling may fork; it runs again on each
 * child until no citizen is {@link #pending()}.
 *
 * <p>Distinct from {@link org.clauseway.logic.constraints.store.Factor#enforce},
 * the per-term commit before reification (labelling), which runs only where an
 * answer is reified; and from a parked suspension, an owed CONDITION, which
 * may ride through a judge and is refused only where an answer leaves.
 */
public interface Enforceable extends Packaged {

	/** True while this store still owes work at the exit. */
	boolean pending();

	/** Settles one step of what is owed; may fork, may leave more pending. */
	Goal enforce();
}
