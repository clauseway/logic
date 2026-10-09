package org.clauseway.logic.goals.optimizer;

// ABOUTME: Prunes doomed branches: a posting refuted under the pass state rewrites
// ABOUTME: to failure, a dead conjunct collapses its conjunction, dead alternatives drop.

import org.clauseway.logic.constraints.Posting;
import org.clauseway.logic.goals.Conde;
import org.clauseway.logic.goals.Conjunction;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.NamedGoal;
import org.clauseway.logic.goals.Knowledge;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;

/**
 * The refutation consumer of {@link Posting#doomed}: a leaf provably failing
 * under the pass state rewrites to {@link Goal#failure()}, and death folds
 * structurally — a dead conjunct kills its whole conjunction, a dead
 * alternative drops from its disjunction (all dead = failure). Doom is a
 * trust surface (never claimed when later knowledge could lift it), so an
 * open posting is left untouched. The kill needs no ordering: pruning and
 * {@link OrderingOptimizer} are separate passes, composed via
 * {@link Optimizer#pipeline}. The package is pass state — empty at the root
 * rewrite, live at the defer hook — so kills sharpen as knowledge arrives.
 */
public class DoomPruner implements Optimizer {

	private final Knowledge bound;

	public DoomPruner() {
		this(Knowledge.empty());
	}

	private DoomPruner(Knowledge bound) {
		this.bound = bound;
	}

	@Override
	public Optimizer with(Knowledge p) {
		return new DoomPruner(p);
	}

	@Override
	public Goal visit(Goal goal) {
		return prune(goal).getGoal();
	}

	@Override
	public Goal visit(Conjunction conjunction) {
		return prune(conjunction).getGoal();
	}

	@Override
	public Goal visit(Conde conde) {
		return prune(conde).getGoal();
	}

	@Override
	public Goal visit(NamedGoal named) {
		return prune(named).getGoal();
	}

	@Value
	private static class Pruned {
		Goal goal;
		boolean dead;
	}

	private Pruned prune(Goal g) {
		if (g instanceof Conjunction) {
			List<Pruned> ps = Optimizer.visitAll(((Conjunction) g).getClauses(), this::prune);
			return ps.stream().anyMatch(Pruned::isDead) ?
					new Pruned(Goal.failure(), true) :
					new Pruned(Conjunction.of(ps.stream()
							.map(Pruned::getGoal)
							.toArray(Goal[]::new)), false);
		}
		if (g instanceof Conde) {
			List<Goal> live = Optimizer.visitAll(((Conde) g).getClauses(), this::prune).stream()
					.filter(p -> !p.isDead())
					.map(Pruned::getGoal)
					.collect(Collectors.toList());
			return live.isEmpty() ?
					new Pruned(Goal.failure(), true) :
					new Pruned(Conde.of(live), false);
		}
		if (g instanceof NamedGoal) {
			NamedGoal named = (NamedGoal) g;
			Pruned p = prune(named.getGoal());
			return new Pruned(NamedGoal.of(named.getLabel(), p.getGoal(), named.getName()), p.isDead());
		}
		if (g instanceof Posting && ((Posting) g).doomed(bound)) {
			return new Pruned(Goal.failure(), true);
		}
		return new Pruned(g, false);
	}
}
