// ABOUTME: Tabling (memoization) of logic goals: the master/consumer skeleton
// ABOUTME: and the definition of tabled relations.

/**
 * Tabling of logic goals. {@link org.clauseway.logic.tabling.Tabling} holds
 * the shared skeleton: the first application of a call becomes the master
 * that runs the body and caches every reified answer, later applications
 * consume the cache and park at the entry's channel until it grows or seals.
 * {@code Tabling.define} and {@code Tabling.defineRecursive} produce a
 * {@link org.clauseway.logic.tabling.Tabled} relation, whose {@code apply}
 * keys the cache on the relation's identity plus the reified arguments.
 * {@link org.clauseway.logic.tabling.TablingMode} is the algorithm plugged
 * into the skeleton (how derivation values fold and when answers are
 * delivered); the table itself and its entries live in
 * {@code org.clauseway.logic.tabling.table}, and a solve obtains one through
 * {@code org.clauseway.logic.solving.Query#tabled}. Pattern lookup for
 * sealed subsumers lives in {@code org.clauseway.logic.tabling.subsumption}.
 */
package org.clauseway.logic.tabling;
