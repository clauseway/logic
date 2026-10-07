package org.clauseway.logic.goals;

// ABOUTME: A value that rides the immutable Knowledge keyed by its class — copied
// ABOUTME: on branch, so backtracking gives each derivation its own isolated copy.

import org.clauseway.logic.tabling.table.Table;

/**
 * Citizenship in the {@link Knowledge}: a payload the package carries through the
 * search, keyed by its concrete class, persistent so each branch keeps its own.
 * This is what {@link org.clauseway.logic.debug.DebugStore the tracer},
 * {@link Table the table}, the optimizer and the
 * mode markers all actually need — carried in the package, not participating
 * in constraint solving. {@link org.clauseway.logic.constraints.store.Factor} is
 * the specialization that does take part in it.
 */
public interface Packaged {
}
