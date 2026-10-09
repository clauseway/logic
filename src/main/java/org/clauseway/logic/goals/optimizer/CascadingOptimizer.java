package org.clauseway.logic.goals.optimizer;

// ABOUTME: The normalization pass: nested conjunctions splice into their parent
// ABOUTME: and nested condes become sibling alternatives, in one bottom-up traversal.


import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Conjunction;
import org.clauseway.logic.goals.Goal;

/**
 * Normalizes a goal tree in one bottom-up pass: children first, then nested
 * {@link Conjunction}s splice into their parent and nested {@link Conde}s
 * become sibling alternatives. Nothing nested survives a single traversal, so
 * no fixpoint iteration is needed — the recursion is the termination argument.
 * Everything else is the neutral walk inherited from {@link Optimizer}.
 */
public class CascadingOptimizer implements Optimizer {

	@Override
	public Goal visit(Conjunction conjunction) {
		if (conjunction.getClauses().isEmpty()) {
			return Goal.success();
		}
		return Conjunction.of(conjunction.getClauses().stream()
				.map(g -> g.accept(this))
				.flatMap(g -> g instanceof Conjunction ?
						((Conjunction) g).getClauses().stream() :
						Stream.of(g))
				.toArray(Goal[]::new));
	}

	@Override
	public Goal visit(Conde conde) {
		if (conde.getClauses().isEmpty()) {
			return Goal.failure();
		}
		return Conde.of(conde.getClauses().stream()
				.map(g -> g.accept(this))
				.flatMap(g -> g instanceof Conde ?
						((Conde) g).getClauses().stream() :
						Stream.of(g))
				.collect(Collectors.toList()));
	}
}
