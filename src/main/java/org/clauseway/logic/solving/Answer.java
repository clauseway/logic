package org.clauseway.logic.solving;

// ABOUTME: The answer artifact, Call's dual: a relation token, the reified
// ABOUTME: image, and the Condition it holds under.

import org.clauseway.logic.unification.terms.Reified;
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
}
