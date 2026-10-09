package org.clauseway.logic.goals.optimizer;

// ABOUTME: A visitor over the goal combinators — the seam for goal-tree rewriting.
// ABOUTME: The generic visit(Goal) overload is the extension hook for foreign goal types.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.clauseway.functional.Exceptions;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.Nothing;
import java.util.Optional;
import java.util.function.BiFunction;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Conjunction;
import org.clauseway.logic.goals.Deferred;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.NamedGoal;
import org.clauseway.logic.goals.Knowledge;

/**
 * Rewrites goal trees before execution. Dispatch is double: goals implement
 * {@link Goal#accept}, combinators route to their own overload, and everything
 * else — opaque lambdas, committed choice, foreign goal types — lands in the
 * generic {@link #visit(Goal)}, which downstream optimizers override to
 * recognise their own goals (a planner's lookup goals, say).
 *
 * <p>Contract: a pass must preserve the binding environment at every goal it
 * does not itself own — reordering is legal only within runs of owned goals
 * between unrecognised barriers. This protects committed choice AND tabling
 * (table entries are keyed on call-argument boundness; moving a binder across
 * a tabled call multiplies its table keys per value).
 *
 * <p>Optimizers compose as an ordered pipeline of passes, never by merging
 * visitors. No pass needs fixpoint iteration: normalization is a single
 * bottom-up traversal.
 *
 * <p>The default visits are the neutral walk: children visited, structure
 * preserved, leaves held in place. A pass overrides only the nodes it acts
 * on; an Optimizer overriding nothing rewrites nothing.
 */
public interface Optimizer {

	/** The fallback and extension hook: anything unrecognised is a barrier. */
	default Fiber<Goal> visit(Goal goal) {
		return Fiber.done(goal);
	}

	default Fiber<Goal> visit(Conjunction conjunction) {
		return visitAll(conjunction.getClauses(), g -> g.accept(this))
				.map(gs -> Conjunction.of(gs.toArray(new Goal[0])));
	}

	default Fiber<Goal> visit(Conde conde) {
		return visitAll(conde.getClauses(), g -> g.accept(this))
				.map(Conde::of);
	}

	/** Transparent: tracing must not disable optimization. */
	default Fiber<Goal> visit(NamedGoal named) {
		return named.getGoal().accept(this)
				.map(g -> NamedGoal.of(named.getLabel(), g, named.getName()));
	}

	default Fiber<Goal> visit(Barrier barrier) {
		return Fiber.done(barrier);
	}

	/** A deferred body is unknown but not opaque: left in place, never a barrier. */
	default Fiber<Goal> visit(Deferred deferred) {
		return Fiber.done(deferred);
	}

	/** Visits every clause in order, collecting the per-clause results. */
	static <T> Fiber<List<T>> visitAll(List<Goal> clauses, Function<Goal, Fiber<T>> visit) {
		return clauses.stream()
				// defer keeps the descent on the fiber trampoline, not the Java stack
				.map(g -> Fiber.defer(() -> visit.apply(g)))
				.reduce(Fiber.done(new ArrayList<>()),
						(acc, r) -> Fiber.zip(acc, r)
								.map(t -> {
									t._1.add(t._2);
									return t._1;
								}),
						Exceptions.throwingBiOp(UnsupportedOperationException::new));
	}

	/**
	 * Pass-state injection: a state-aware pass returns a copy carrying
	 * {@code p}; static passes ignore it. Called by {@link OptimizerStore} at
	 * the defer hook with the live state.
	 */
	default Optimizer with(Knowledge p) {
		return this;
	}

	/**
	 * A {@link Barrier} is being entered with {@code p}: the goal behind it is
	 * about to be partitioned from whatever the pass parked in the package. A
	 * pass that parked state discharges it here; empty means nothing to do,
	 * and the barrier is straight through.
	 */
	default Optional<Cont<Knowledge, Nothing>> entering(Knowledge p) {
		return Optional.empty();
	}

	/**
	 * An answer is leaving a {@link Barrier} as {@code p}: whatever the pass
	 * parked in it would leave with it. Discharged here; empty means nothing to
	 * do, and the emission is straight through.
	 */
	default Optional<Cont<Knowledge, Nothing>> leaving(Knowledge p) {
		return Optional.empty();
	}

	/** Sequential composition — passes compose as a pipeline, never by merging. */
	static Optimizer pipeline(Optimizer... optimizers) {
		return new Optimizer() {
			private Fiber<Goal> all(Goal g) {
				return Arrays.stream(optimizers)
						.reduce(Fiber.done(g),
								(acc, o) -> acc.flatMap(g1 -> g1.accept(o)),
								Exceptions.throwingBiOp(UnsupportedOperationException::new));
			}

			@Override
			public Fiber<Goal> visit(Goal goal) {
				return all(goal);
			}

			@Override
			public Fiber<Goal> visit(Conjunction conjunction) {
				return all(conjunction);
			}

			@Override
			public Fiber<Goal> visit(Conde conde) {
				return all(conde);
			}

			@Override
			public Fiber<Goal> visit(NamedGoal named) {
				return all(named);
			}

			@Override
			public Fiber<Goal> visit(Barrier barrier) {
				return all(barrier);
			}

			@Override
			public Fiber<Goal> visit(Deferred deferred) {
				return all(deferred);
			}

			@Override
			public Optimizer with(Knowledge p) {
				return pipeline(Arrays.stream(optimizers)
						.map(o -> o.with(p))
						.toArray(Optimizer[]::new));
			}

			@Override
			public Optional<Cont<Knowledge, Nothing>> entering(Knowledge p) {
				return each(p, Optimizer::entering);
			}

			@Override
			public Optional<Cont<Knowledge, Nothing>> leaving(Knowledge p) {
				return each(p, Optimizer::leaving);
			}

			/** Each pass in turn, the next one over the previous one's emissions. */
			private Optional<Cont<Knowledge, Nothing>> each(Knowledge p,
					BiFunction<Optimizer, Knowledge, Optional<Cont<Knowledge, Nothing>>> side) {
				Optional<Cont<Knowledge, Nothing>> crossed = Optional.empty();
				for (Optimizer o : optimizers) {
					crossed = crossed
							.map(c -> c.flatMap(k -> side.apply(o, k).orElseGet(() -> Cont.just(k))))
							.map(Optional::of)
							.orElseGet(() -> side.apply(o, p));
				}
				return crossed;
			}
		};
	}
}
