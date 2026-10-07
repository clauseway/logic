// ABOUTME: The solving entry point: Query seeds a root Knowledge through slots and runs a
// ABOUTME: goal; Selection/Row read answers, Answer/Call/Condition/Residues name them.

/**
 * Running a goal and reading its answers. {@link org.clauseway.logic.solving.Query}
 * is the only way to run a {@link org.clauseway.logic.goals.Goal}: {@code Query.of(goal)}
 * plus the slot methods ({@code from}, {@code tabled}, {@code on}, {@code weighted},
 * {@code traced}, {@code profiled}, {@code optimized}, {@code slot}) seeds the root
 * {@link org.clauseway.logic.goals.Knowledge}; {@code run()} is the
 * {@code Cont} primitive, {@code solve(out)} the reified reading, {@code stream()}
 * the lazy pull of every derived state, and {@code select(vars)} opens a
 * {@link org.clauseway.logic.solving.Selection} whose {@code rows()} stream
 * {@link org.clauseway.logic.solving.Row}s. The answer vocabulary lives beside it:
 * {@link org.clauseway.logic.solving.Answer} (a reified image under a
 * {@link org.clauseway.logic.solving.Condition}) is the dual of
 * {@link org.clauseway.logic.solving.Call} (reified arguments under one
 * {@link org.clauseway.logic.solving.Residues} region); {@code Condition} is the
 * bounded semiring of regions, {@link org.clauseway.logic.solving.JoinMap} the
 * keyed join-semilattice that folds answers by it, and
 * {@link org.clauseway.logic.solving.Subsumption} the Herbrand matching behind
 * {@code Call.subsumes}.
 *
 * <p>{@code org.clauseway.logic.tabling} consumes this vocabulary (Call, Answer,
 * Condition, Residues, {@code Answer.capture}) without going through {@code Query};
 * the stores that the slots plant come from {@code org.clauseway.logic.debug},
 * {@code org.clauseway.logic.tabling.table}, {@code org.clauseway.logic.weight} and
 * {@code org.clauseway.logic.goals.optimizer}; enforcement and reification run in
 * {@code org.clauseway.logic.constraints}.
 */
package org.clauseway.logic.solving;
