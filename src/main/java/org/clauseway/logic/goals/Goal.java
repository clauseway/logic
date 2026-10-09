package org.clauseway.logic.goals;

import static org.clauseway.functional.Nothing.nothing;
import static org.clauseway.functional.fibers.Fiber.done;

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.logic.goals.optimizer.Bounded;
import org.clauseway.logic.goals.optimizer.Optimizer;
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
 * and combinators (e.g., {@link #and(Goal...)}, {@link #or(Goal...)});
 * {@link org.clauseway.logic.solving.Query} executes a goal and retrieves its solutions.
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
	 * The soft cut: this goal, or else the first of {@code goals} that has an
	 * answer — committing to that clause and keeping EVERY answer it has
	 * (miniKanren's conda; {@link Conda}).
	 *
	 * @param goals Alternative goals, tried in order.
	 * @return A {@link Conda} over this goal and the alternatives.
	 * @see #conda(Goal...)
	 */
	default Goal orElse(Goal... goals) {
		return new Conda().orElse(this).orElse(goals);
	}

	/**
	 * Committed choice: this goal, or else the first of {@code goals} that has an
	 * answer — committing to that clause and keeping ONE answer of it
	 * (miniKanren's condu; {@link Condu}).
	 *
	 * @param goals Alternative goals, tried in order.
	 * @return A {@link Condu} over this goal and the alternatives.
	 * @see #condu(Goal...)
	 */
	default Goal orElseFirst(Goal... goals) {
		return new Condu().orElseFirst(this).orElseFirst(goals);
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
	 * Committed choice (miniKanren's condu): the first of {@code goals} that has
	 * an answer is committed to, and ONE of its answers is kept. Each clause is
	 * one goal, run whole: the commit is over the clause's answers, not over a
	 * separate question. No goals is {@link #failure()}.
	 *
	 * @param goals The clauses, tried in order.
	 * @return A {@link Condu} over the clauses.
	 */
	static Goal condu(Goal... goals) {
		return Arrays.stream(goals)
				.reduce(Goal::orElseFirst)
				.orElseGet(Goal::failure);
	}

	/**
	 * The soft cut (miniKanren's conda): the first of {@code goals} that has an
	 * answer is committed to, and EVERY answer it has is kept. Each clause is
	 * one goal, run whole: the commit is over the clause's answers, not over a
	 * separate question. No goals is {@link #failure()}.
	 *
	 * @param goals The clauses, tried in order.
	 * @return A {@link Conda} over the clauses.
	 */
	static Goal conda(Goal... goals) {
		return Arrays.stream(goals)
				.reduce(Goal::orElse)
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
	 * @return A {@link Deferred} goal, named for the trace.
	 */
	static Goal defer(Supplier<Goal> g) {
		return new Deferred(g).named("recursive call");
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