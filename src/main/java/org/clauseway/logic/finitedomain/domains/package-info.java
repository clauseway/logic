// ABOUTME: The concrete shapes of a finite domain: empty, one value, an interval,
// ABOUTME: a union of disjoint members, and an explicit enumerated set.

/**
 * The closed set of {@code org.clauseway.logic.finitedomain.Domain} shapes.
 * {@link org.clauseway.logic.finitedomain.domains.Empty} is the lattice
 * bottom, {@link org.clauseway.logic.finitedomain.domains.Singleton} holds
 * one value, {@link org.clauseway.logic.finitedomain.domains.Interval} is a
 * range between two {@code Bound}s,
 * {@link org.clauseway.logic.finitedomain.domains.Union} keeps disjoint
 * members sorted and merged where they touch, and
 * {@link org.clauseway.logic.finitedomain.domains.EnumeratedDomain} is a
 * sorted, duplicate-free array of values. Every shape keeps a canonical
 * spelling so that equal value sets compare equal, which the equal-domain
 * termination guard relies on.
 * {@link org.clauseway.logic.finitedomain.domains.DomainVisitor} dispatches
 * over the five shapes through {@code Domain.accept}. Domains are built by the
 * shapes' own {@code of} factories and by the methods of {@code Domain}; the
 * relations that narrow them live in
 * {@code org.clauseway.logic.finitedomain.relations}.
 */
package org.clauseway.logic.finitedomain.domains;
