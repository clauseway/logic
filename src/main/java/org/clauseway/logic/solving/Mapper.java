package org.clauseway.logic.solving;

// ABOUTME: The mapped reading of a select: the variables a Java value needs,
// ABOUTME: and the mapping of one solution over them into that value.

import org.clauseway.logic.unification.terms.Unifiable;

/**
 * A Java value's two faces at the engine's boundary: {@link #keys} are the
 * variables it is built from, which a goal binds, and {@link #map} builds it
 * from one solution over them. {@link Query#select(Mapper)} selects the keys
 * and maps every solution; a solution with an open cell is refused before
 * {@code map} sees it, because a Java value has no place for one.
 *
 * @param <D> the value a solution maps into
 */
public interface Mapper<D> {

	/** The variables the value is built from: the select's keys. */
	Iterable<? extends Unifiable<?>> keys();

	/** The value one solution over the keys builds; every cell is decided. */
	D map(Solution solution);
}
