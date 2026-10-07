// ABOUTME: The nogood store: forbidden conjunctions of postings, verified by
// ABOUTME: trial against the rest of the package after every binding.

/**
 * Nogoods: a {@link org.clauseway.logic.nogoods.Nogood} states that a
 * conjunction of {@code Posting} literals must not all hold at once, and
 * {@link org.clauseway.logic.nogoods.NogoodConstraints} is the constraint
 * store that keeps them as a theory. {@link org.clauseway.logic.nogoods.Exclusion}
 * is the entry point: {@code Exclusion.exclude(literals)} posts a nogood
 * through the chokepoint in {@code org.clauseway.logic.constraints}.
 * {@link org.clauseway.logic.nogoods.Verification} re-verifies every nogood
 * against a scratch package after each revision, discarding entailed ones
 * and failing on refuted ones; the store implements
 * {@code org.clauseway.logic.constraints.store.Verifier}, so the driver folds
 * it after every value store. Disequality is the one-literal case.
 */
package org.clauseway.logic.nogoods;
