package org.clauseway.logic.solving;

// ABOUTME: The answer artifact, Call's dual: a relation token, the reified
// ABOUTME: image, and the Condition it holds under.

import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.constraints.Propagation;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import lombok.Value;

/**
 * One answer of a relation, whole: its reified image — a ground cell is a
 * value, a free cell an Any (the ∀-schema marking) — and the
 * {@link Condition} it holds under, unconditional answers at
 * {@link Condition#ONE}.
 *
 * The dual of {@link Call}: a call names ONE region it asks about (its
 * {@link Residues} conjunct), an answer may hold under a DISJUNCTION of
 * regions, grown by ⊕ as derivations arrive. The relation slot is generic
 * exactly as {@link Call}'s: {@code R} is any identity token naming the
 * relation — equal tokens name one relation.
 */
@Value(staticConstructor = "of")
public class Answer<R> {
	R relation;
	Reified<?> reified;
	Condition condition;

	/**
	 * The guard dropped: asserts UNCONDITIONALLY what was derived under a
	 * condition — a deliberate strengthening of the claim, owned entirely
	 * by the caller, never performed silently. The explicit bridge from a
	 * caveated read to strict write doors.
	 */
	public Answer<R> unconditional() {
		return new Answer<>(relation, reified, Condition.ONE);
	}

	/**
	 * RAW capture: the anchor's state in one answer {@link Knowledge} — the
	 * walked image plus the residual knowledge conditioning it
	 * ({@link Residues#all}), arriving as a one-region condition. No
	 * enforcement runs: this is the produce seam's operation, where a cache
	 * or a data plane wants the REGION an answer denotes, labelling
	 * deferred to consumption. A user-facing ask must commit the stores
	 * first (Constraints.enforced) or wide answers smuggle un-enforced
	 * knowledge into their conditions. REFUSES under pending suspensions:
	 * a parked suspension is a condition the answer still owes, and the
	 * owed condition cannot ride the answer.
	 */
	public static <R> Fiber<Answer<R>> capture(R token, Knowledge answer, Unifiable<?> anchor) {
		if (Propagation.suspensionsPending(answer)) {
			throw new IllegalStateException(
					"an answer may not be asked while suspensions pend: "
							+ "the owed condition cannot ride the answer");
		}
		return Residues.all(answer, anchor)
				.map(pair -> Answer.of(token, pair._1, Condition.of(pair._2)));
	}
}
