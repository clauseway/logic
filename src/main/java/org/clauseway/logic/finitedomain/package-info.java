// ABOUTME: Finite-domain constraints: domain membership, order and arithmetic
// ABOUTME: relations over any value type that supplies the required capabilities.

/**
 * Finite-domain constraints. {@link org.clauseway.logic.finitedomain.FiniteDomain}
 * is the generic entry point: each method takes the {@code Comparator},
 * {@code Arithmetic}, {@code Multiplicative} and {@code Discrete} instances it
 * needs and returns a {@code Posting}; the typed fronts
 * {@link org.clauseway.logic.finitedomain.Ints},
 * {@link org.clauseway.logic.finitedomain.Longs},
 * {@link org.clauseway.logic.finitedomain.BigIntegers},
 * {@link org.clauseway.logic.finitedomain.BigDecimals},
 * {@link org.clauseway.logic.finitedomain.Dates} and
 * {@link org.clauseway.logic.finitedomain.Instants} supply those instances for
 * their value type. {@link org.clauseway.logic.finitedomain.Domain} is the
 * value set a variable may range over, built from
 * {@link org.clauseway.logic.finitedomain.Bound} endpoints; its shapes live in
 * {@code org.clauseway.logic.finitedomain.domains}.
 * {@link org.clauseway.logic.finitedomain.FiniteDomainConstraints} is the
 * constraint store, a {@code org.clauseway.logic.lattice.LatticeFactor} over
 * {@code Domain}; the relations it administers live in
 * {@code org.clauseway.logic.finitedomain.relations} and the capability
 * interfaces in {@code org.clauseway.logic.finitedomain.capabilities}.
 */
package org.clauseway.logic.finitedomain;
