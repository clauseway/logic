// ABOUTME: The answer table of one solve: entries per tabled call, their answer
// ABOUTME: cells, and the readers that consume them.

/**
 * The table a solve carries in its package.
 * {@link org.clauseway.logic.tabling.table.Table} maps each tabled
 * {@code Call} (relation plus reified arguments) to its
 * {@link org.clauseway.logic.tabling.table.TableEntry} and holds the solve's
 * {@code TablingMode}; {@code Table.empty()} builds the plain streaming table,
 * {@code Table.of(mode)} one over any mode, and
 * {@code org.clauseway.logic.solving.Query#tabled} seeds it into the root
 * package. A {@code TableEntry} is the answer cell of one call: a
 * {@code JoinMap} from reified answer terms to their folded values, the
 * channel consumers park at, and the seal that marks it complete. A
 * {@link org.clauseway.logic.tabling.table.Reader} bundles a consuming
 * frame's continuation, state, arguments and log cursor. The package-private
 * {@code Streaming} mode delivers each answer as soon as its value is final;
 * the closed (star) mode lives in {@code org.clauseway.logic.weight}. The
 * master/consumer skeleton that drives these types is
 * {@code org.clauseway.logic.tabling.Tabling}.
 */
package org.clauseway.logic.tabling.table;
