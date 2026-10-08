package org.clauseway.logic.goals;

// ABOUTME: A goal whose body is built on application — the recursive unfolding
// ABOUTME: behind Goal.defer, visible to an Optimizer as its own leaf.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.logic.goals.optimizer.Optimizer;
import org.clauseway.logic.goals.optimizer.OptimizerStore;
import java.util.function.Supplier;
import lombok.Value;

/**
 * The goal {@link Goal#defer} returns: its body is supplied only when the goal
 * is applied, which is what lets a relation refer to itself without building
 * an infinite tree. When the package carries an {@link OptimizerStore}, the
 * body is rewritten through it before it runs.
 *
 * <p>Being its own type is a contract: what is deferred is a relation body,
 * so an optimizer may treat the leaf as transparent widening — unknown order,
 * movable, never a barrier — instead of the opaque lambda it would otherwise be.
 */
@Value
public class Deferred implements Goal {
	Supplier<Goal> body;

	@Override
	public Cont<Knowledge, Nothing> apply(Knowledge s) {
		return OptimizerStore.from(s)
				.map(store -> Cont.<Knowledge, Nothing> defer(() ->
						store.rewrite(body.get(), s)
								.map(unfolded -> unfolded.apply(s))))
				.getOrElse(() -> body.get().apply(s));
	}

	@Override
	public Fiber<Goal> accept(Optimizer optimizer) {
		return optimizer.visit(this);
	}

	@Override
	public String toString() {
		return "deferred";
	}
}
