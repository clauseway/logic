// ABOUTME: Generalization retrieval over reified call patterns: a trie keyed by
// ABOUTME: the preorder edge walk of a pattern, queried by specific terms.

/**
 * Subsumption lookup over reified patterns.
 * {@link org.clauseway.logic.tabling.subsumption.SubsumptionMap} is the entry
 * point: {@code put} stores a value under a general pattern (one that may
 * carry pattern variables) and {@code subsumers} returns every stored value
 * whose pattern subsumes a specific query term. A stored pattern is
 * serialized to its preorder walk of
 * {@link org.clauseway.logic.tabling.subsumption.Edge} labels (an atom, a
 * branch with its arity, or an any) over a package-private persistent trie;
 * candidates found by the walk are confirmed with
 * {@code org.clauseway.logic.solving.Subsumption#subsumes}.
 * {@code org.clauseway.logic.tabling.table.Table} keeps one map per relation
 * to find a sealed entry whose call pattern generalizes an incoming call.
 */
package org.clauseway.logic.tabling.subsumption;
