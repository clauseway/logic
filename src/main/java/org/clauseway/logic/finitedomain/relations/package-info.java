// ABOUTME: The finite-domain propagators: order, disequality and arithmetic
// ABOUTME: relations over watched terms, plus the toolkit their verdicts share.

/**
 * The propagators the finite-domain store administers, each a
 * {@code org.clauseway.logic.lattice.Propagator} over
 * {@code FiniteDomainConstraints}: {@link org.clauseway.logic.finitedomain.relations.Leq}
 * ({@code less <= more}), {@link org.clauseway.logic.finitedomain.relations.Lss}
 * ({@code less < more}), {@link org.clauseway.logic.finitedomain.relations.Separate}
 * ({@code l != r}), {@link org.clauseway.logic.finitedomain.relations.Add}
 * ({@code point + delta = shifted}) and
 * {@link org.clauseway.logic.finitedomain.relations.Mul} ({@code a * b = rhs}).
 * They are constructed by the factory methods of
 * {@code org.clauseway.logic.finitedomain.FiniteDomain} and posted through
 * {@code Propagation.activate}; callers do not instantiate them directly.
 * {@link org.clauseway.logic.finitedomain.relations.Operators} holds the shared
 * toolkit (the domain gate, ground minting, bound arithmetic, hull selection)
 * and {@code DomainUpdate} turns a set of narrowed domains into the store's
 * {@code Update}, carrying the equal-domain termination guard.
 */
package org.clauseway.logic.finitedomain.relations;
