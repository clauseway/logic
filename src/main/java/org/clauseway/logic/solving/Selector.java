package org.clauseway.logic.solving;

// ABOUTME: What a select selects, as a Java value: the variables it is built from,
// ABOUTME: and the mapping of one row over them into that value.

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.clauseway.logic.unification.terms.Unifiable;

/**
 * A Java value's two faces at the engine's boundary: {@link #keys} are the
 * variables it is built from, which a goal binds, and {@link #map} builds it
 * from one row over them. {@link Query#select(Selector)} selects the keys
 * and maps every row; a cell that is not ground refuses at
 * {@link Row#get}, because a Java value has no place for one, and the
 * refusal shows the whole row with each cell under the name
 * {@link #names} gives its key. {@link Selection#rows()} keeps the rows
 * unmapped, for a reading this one is not.
 *
 * @param <D> the value a row maps into
 */
public interface Selector<D> {

	/** The variables the value is built from: the select's keys. */
	Iterable<? extends Unifiable<?>> keys();

	/** The names a row shows the keys by, in key order: the keys themselves unless the value names them. */
	default Iterable<String> names() {
		return StreamSupport.stream(keys().spliterator(), false)
				.map(Object::toString)
				.collect(Collectors.toList());
	}

	/** The value one row over the keys builds; {@link Row#get} at every cell it needs. */
	D map(Row row);
}
