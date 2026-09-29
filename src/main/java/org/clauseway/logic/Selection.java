package org.clauseway.logic;

// ABOUTME: The middle of the select pipeline, where the user chooses the
// ABOUTME: reading: extraction (enforced default, raw explicit) and
// ABOUTME: multiplicity (distinct default, all explicit); rows() streams.

import static org.clauseway.logic.unification.terms.LVal.lval;

import org.clauseway.functional.Exceptions;
import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.tuples.Tuple;
import org.clauseway.logic.constraints.Constraints;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.solving.Answer;
import org.clauseway.logic.solving.Condition;
import org.clauseway.logic.solving.JoinMap;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * A {@link Query#select} with its reading still open — the two switches are
 * the user's, because different consumers want different answers from the
 * same question:
 *
 * <ul>
 * <li>EXTRACTION — {@code enforced} (default): stores commit their
 * constraints about the selected variables before capture, the same stage a
 * regular reify runs; domains label into ground rows, surviving residues
 * guard them. {@link #raw()}: capture the REGION each derivation denotes,
 * labelling deferred — the produce seam's reading (caches, data planes).</li>
 * <li>MULTIPLICITY — {@code distinct} (default): alpha-equal images fold
 * into one row, conditions joined by ⊕. {@link #all()}: one row per
 * derivation, duplicates included — the derivation view, streamed lazily.</li>
 * </ul>
 *
 * {@link #rows()} streams under the query's driver; closing the stream
 * closes the driver. The distinct reading needs exhaustion, so its stream
 * is computed at the call; the all reading stays lazy.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Selection {

	private final Query query;
	private final List<Unifiable<?>> keys;
	private final boolean raw;
	private final boolean bag;

	/** Capture regions instead of enforcing: the produce seam's reading. */
	public Selection raw() {
		return new Selection(query, keys, true, bag);
	}

	/** One row per derivation, duplicates included: the derivation view. */
	public Selection all() {
		return new Selection(query, keys, raw, true);
	}

	/** The rows, under the chosen reading. */
	public Stream<Row> rows() {
		Unifiable<Object> anchor = lval(Tuple.ofAll(
				keys.stream().map(Unifiable::getObjectTerm).toArray()));
		Cont<Answer<Unifiable<?>>, Nothing> answers = Cont.suspend(k -> query.run()
				.apply(world -> extracted(world, anchor, k)));
		if (bag) {
			return Query.harvest(answers, query.factory()).map(a -> new Row(keys, a));
		}
		try (Stream<Answer<Unifiable<?>>> all = Query.harvest(answers, query.factory())) {
			JoinMap<Reified<?>, Condition> folded = all.reduce(
					JoinMap.empty(Condition.RING),
					(map, a) -> map.append(a.getReified(), a.getCondition()).getOrElse(map),
					Exceptions.throwingBiOp(UnsupportedOperationException::new));
			return IntStream.range(0, folded.size())
					.mapToObj(folded::get)
					.map(entry -> new Row(keys,
							Answer.of((Unifiable<?>) anchor, entry._1, entry._2)))
					.collect(Collectors.toList())
					.stream();
		}
	}

	private Fiber<Nothing> extracted(Knowledge world, Unifiable<Object> anchor,
			Fiber.Fn<Answer<Unifiable<?>>, Nothing> k) {
		if (raw) {
			return Answer.<Unifiable<?>> capture(anchor, world, anchor).flatMap(k::apply);
		}
		return Constraints.enforced(world, anchor)
				.apply(committed -> Answer.<Unifiable<?>> capture(anchor, committed, anchor)
						.flatMap(k::apply));
	}
}
