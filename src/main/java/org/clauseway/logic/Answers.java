package org.clauseway.logic;

// ABOUTME: The ask stage of a Query: one Answer per derivation (the bag,
// ABOUTME: derivation view), the token routing door, and the bare-values
// ABOUTME: face that refuses guarded answers instead of dropping their
// ABOUTME: conditions.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.solving.Answer;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * A {@link Query}'s answers about one anchor: per answer knowledge, the
 * anchor's walked image plus the residual knowledge conditioning it — an
 * {@link Answer} tagged with this ask's token. Duplicates ride: one
 * element per DERIVATION, the view weighted counting needs; folding to
 * answer-set semantics is a downstream choice, never a default.
 *
 * <p>{@link #each} is the primitive face — caller-driven, engine-internal
 * consumers stay in the fiber world. {@link #stream} harvests under the
 * query's driver. {@link #values} is the lossy convenience: bare reified
 * values, REFUSING any answer whose condition is not
 * {@link org.clauseway.logic.solving.Condition#ONE} — dropping a guard is
 * {@link Answer#unconditional()}'s explicit deed, never a stream's silent
 * one.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Answers<R, T> {

	private final Query query;
	private final R token;
	private final Unifiable<T> anchor;

	/** The primitive: one answer per derivation, driven by the caller. */
	public Cont<Answer<R>, Nothing> each() {
		return Cont.suspend(k -> query.run()
				.apply(knowledge -> Answer.of(token, knowledge, anchor).flatMap(k::apply)));
	}

	/** The pull harvest of {@link #each} under the query's driver. */
	public Stream<Answer<R>> stream() {
		return Query.harvest(each(), query.factory());
	}

	/** Bare values; a guarded answer refuses instead of shedding its condition. */
	@SuppressWarnings("unchecked")
	public Stream<Reified<T>> values() {
		return stream().map(answer -> {
			if (!answer.getCondition().isOne()) {
				throw new IllegalStateException(
						"a guarded answer cannot drop to a bare value: " + answer.getReified()
								+ " holds under " + answer.getCondition()
								+ " — consume answers with their conditions, or decide the"
								+ " guard explicitly (unconditional())");
			}
			return (Reified<T>) answer.getReified();
		});
	}
}
