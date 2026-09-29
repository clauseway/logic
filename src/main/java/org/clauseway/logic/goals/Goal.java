package org.clauseway.logic.goals;

import static org.clauseway.functional.Nothing.nothing;
import static org.clauseway.functional.fibers.Fiber.done;

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.optimizer.Bounded;
import org.clauseway.logic.goals.optimizer.Optimizer;
import org.clauseway.logic.goals.optimizer.OptimizerStore;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a goal in a logic programming system.
 * <pre>
 * A goal is essentially a function that takes a state ({@link Knowledge}, representing
 * substitutions and variable bindings) and returns a continuation ({@link Cont}).
 * This continuation, when run, will produce zero or more resulting states if the goal
 * is satisfied, or indicate failure.
 *
 * Goals can be combined using logical operators like AND (conjunction) and OR (disjunction)
 * to form more complex goals. They can be solved to find instantiations of variables
 * that satisfy the logical conditions.
 *
 * This interface provides factory methods for common goals (e.g., {@link #success()}, {@link #failure()}),
 * combinators (e.g., {@link #and(Goal...)}, {@link #or(Goal...)}), and methods to execute
 * the goal and retrieve solutions (e.g., {@link #solve(Unifiable)}).
 * </pre>
 *
 * @author TGa
 */
public interface Goal extends Function<Knowledge, Cont<Knowledge, Nothing>> {

	/**
	 * A static factory method that simply returns the provided goal.
	 * <pre>
	 * This can be useful for type inference or when a Goal instance is needed
	 * from a lambda or method reference that already produces one.
	 * </pre>
	 *
	 * @param g The goal to return.
	 * @return The same goal {@code g}.
	 */
	static Goal goal(Goal g) {
		return g;
	}

	/**
	 * Creates a new goal representing the logical conjunction (AND) of this goal
	 * and the provided goals.
	 * <pre>
	 * The resulting goal succeeds if and only if this goal and all specified {@code goals} succeed.
	 * </pre>
	 *
	 * @param goals Additional goals to conjunctively combine with this goal.
	 * @return A new {@link Goal} representing the conjunction.
	 * @see Conjunction#and(Goal...)
	 */
	default Goal and(Goal... goals) {
		Goal[] all = new Goal[goals.length + 1];
		all[0] = this;
		System.arraycopy(goals, 0, all, 1, goals.length);
		return Conjunction.of(all);
	}

	/**
	 * Creates a new goal representing the logical disjunction (OR) of this goal
	 * and the provided goals.
	 * <pre>
	 * The resulting goal succeeds if this goal or any of the specified {@code goals} succeed.
	 * This typically implements a fair disjunction (like {@code conde}).
	 * </pre>
	 *
	 * @param goals Additional goals to disjunctively combine with this goal.
	 * @return A new {@link Goal} representing the disjunction.
	 * @see Conde#or(Goal...)
	 * @see #conde(Goal...)
	 */
	default Goal or(Goal... goals) {
		return new Conde().or(this).or(goals);
	}

	/**
	 * Creates a new goal that tries this goal first, and if it fails, tries the
	 * specified {@code goals} in order.
	 * <pre>
	 * This is often used for committed choice or if-then-else like constructs.
	 * This uses {@link Condu} for its underlying mechanism, specifically its {@code orElse} method.
	 * </pre>
	 *
	 * @param goals Alternative goals to try if the preceding ones fail.
	 * @return A new {@link Goal} representing the ordered disjunction.
	 * @see Condu#orElse(Goal...)
	 * @see #condu(Goal...)
	 */
	default Goal orElse(Goal... goals) {
		return new Condu().orElse(this).orElse(goals);
	}

	/**
	 * Creates a new goal that chains this goal with subsequent {@code goals} using a
	 * "first-match" or "committed-choice" strategy, as provided by {@link Condu#orElseFirst(Goal...)}.
	 * <pre>
	 * It attempts goals in sequence, and the behavior regarding commitment to the first
	 * successful path is determined by the {@code Conda} implementation's {@code orElseFirst} method.
	 * </pre>
	 *
	 * @param goals Alternative goals to try, subject to the "orElseFirst" semantics of {@link Condu}.
	 * @return A new {@link Goal} based on {@link Conda#orElseFirst(Goal...)}.
	 * @see Conda#orElseFirst(Goal...)
	 * @see #conda(Goal...)
	 */
	default Goal orElseFirst(Goal... goals) {
		return new Conda().orElseFirst(this).orElseFirst(goals);
	}

	/**
	 * Creates a goal representing a fair disjunction (logical OR) of the provided goals.
	 * <pre>
	 * All branches that lead to success are explored.
	 * If no goals are provided, it results in a {@link #failure()} goal.
	 * This is constructed by reducing the goals using {@link Goal#or(Goal...)}.
	 * </pre>
	 *
	 * @param goals The goals to be combined disjunctively.
	 * @return A new {@link Goal} representing the fair disjunction (conde).
	 */
	static Goal conde(Goal... goals) {
		return Arrays.stream(goals)
				.reduce(Goal::or)
				.orElseGet(Goal::failure);
	}

	/**
	 * Creates a goal representing a committed choice disjunction of the provided goals.
	 * <pre>
	 * It tries goals in order and typically commits to the first one (or set of them)
	 * that leads to a solution, based on the behavior of {@link Goal#orElse(Goal...)}.
	 * If no goals are provided, it results in a {@link #failure()} goal.
	 * </pre>
	 *
	 * @param goals The goals to be combined.
	 * @return A new {@link Goal} representing the committed choice disjunction (condu).
	 */
	static Goal condu(Goal... goals) {
		return Arrays.stream(goals)
				.reduce(Goal::orElse)
				.orElseGet(Goal::failure);
	}

	/**
	 * Creates a goal representing a conditional disjunction, often used for if-then-else
	 * style logic or "guarded" clauses.
	 * <pre>
	 * It combines the provided goals using the {@link Goal#orElseFirst(Goal...)} strategy.
	 * This means it will try to satisfy the goals in a sequence, and the overall behavior
	 * (e.g., committing to the first successful clause) is determined by the
	 * {@code orElseFirst} logic.
	 * If no goals are provided, it results in a {@link #failure()} goal.
	 * </pre>
	 *
	 * @param goals The goals, often structured as condition-consequence clauses, to be combined.
	 * @return A new {@link Goal} representing the conditional disjunction (conda).
	 */
	static Goal conda(Goal... goals) {
		return Arrays.stream(goals)
				.reduce(Goal::orElseFirst)
				.orElseGet(Goal::failure);
	}

	/**
	 * Creates a goal representing the logical conjunction (AND) of all provided goals.
	 * <pre>
	 * The resulting goal succeeds if and only if all specified {@code goals} succeed.
	 * </pre>
	 *
	 * @param goals The goals to be combined conjunctively.
	 * @return A new {@link Goal} representing the conjunction.
	 */
	static Goal all(Goal... goals) {
		return new Conjunction().and(goals);
	}

	/**
	 * Assigns a name to this goal.
	 * <pre>
	 * Useful for debugging and tracing goal execution.
	 * </pre>
	 *
	 * @param name The name to assign to this goal.
	 * @return A {@link NamedGoal} wrapping this goal with the given name.
	 */
	default Goal named(String name) {
		return NamedGoal.of(pkg -> name, this, name);
	}

	/**
	 * Names this goal with a label rendered against the current state, so a
	 * trace can show the goal's arguments walked to their bindings at each port.
	 *
	 * @param label Renders the goal's label from the package it is applied to.
	 * @return A {@link NamedGoal} whose label is computed per port.
	 */
	default Goal named(Function<Knowledge, String> label) {
		return NamedGoal.of(label, this, null);
	}

	/**
	 * Dispatch into an {@link Optimizer}. Combinators override this to route to
	 * their own overload; everything else — including plain lambda goals — lands
	 * in the generic {@code visit(Goal)} and is a barrier by construction.
	 */
	default Fiber<Goal> accept(Optimizer optimizer) {
		return optimizer.visit(this);
	}

	/**
	 * Defers the creation of a goal, typically used for defining recursive goals.
	 * <pre>
	 * The supplier function {@code g} is only called when the goal is applied,
	 * preventing infinite recursion during goal construction.
	 * The deferred goal is automatically named "recursive call".
	 * </pre>
	 *
	 * @param g A {@link Supplier} that provides the goal to be executed.
	 * @return A new {@link Goal} that defers the creation of the actual goal.
	 */
	static Goal defer(Supplier<Goal> g) {
		return goal(s -> OptimizerStore.from(s)
				.map(store -> Cont.<Knowledge, Nothing> defer(() ->
						store.rewrite(g.get(), s)
								.map(body -> body.apply(s))))
				.getOrElse(() -> g.get().apply(s)))
				.named("recursive call");
	}

	/**
	 * Creates a goal that succeeds if the given boolean condition is true,
	 * and fails otherwise.
	 *
	 * @param bool The boolean condition.
	 * @return {@link #success()} if {@code bool} is true, {@link #failure()} otherwise.
	 */
	static Goal successIf(boolean bool) {
		return bool ?
				success() :
				failure();
	}

	/**
	 * Creates a goal that always succeeds.
	 * <pre>
	 * When applied, it returns a continuation that yields the input state unchanged.
	 * It is named "success".
	 * </pre>
	 *
	 * @return A {@link Goal} that always succeeds.
	 */
	static Goal success() {
		return Bounded.of(1, goal(Cont::just)
				.named("success"));
	}

	/**
	 * Creates a goal that always fails.
	 * <pre>
	 * When applied, it returns a continuation that yields no results.
	 * It is named "failure".
	 * </pre>
	 *
	 * @return A {@link Goal} that always fails.
	 */
	static Goal failure() {
		return Bounded.of(0, goal(s -> k -> done(nothing()))
				.named("failure"));
	}

}