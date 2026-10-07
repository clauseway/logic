package org.clauseway.logic.finitedomain.domains;

// ABOUTME: Visitor over the closed set of Domain shapes: Empty, Singleton,
// ABOUTME: Interval, Union and EnumeratedDomain dispatch to one overload each.

/**
 * Visitor over the closed set of {@link org.clauseway.logic.finitedomain.Domain}
 * shapes: {@link Empty}, {@link Singleton}, {@link Interval}, {@link Union} and
 * {@link EnumeratedDomain}. A domain dispatches to the matching overload from
 * {@code Domain.accept(DomainVisitor)}, so the visitor is the one place where
 * shape-dependent logic lives without instanceof chains.
 *
 * @param <T> the element type of the visited domain
 * @param <R> the result type of a visit
 */
public interface DomainVisitor<T, R> {
	R visit(Empty<T> domain);

	R visit(Singleton<T> domain);

	R visit(Interval<T> domain);

	R visit(Union<T> domain);

	R visit(EnumeratedDomain<T> domain);
}
