package org.clauseway.logic.goals;

import static org.clauseway.functional.Nothing.nothing;
import static org.clauseway.functional.fibers.Fiber.done;

import org.clauseway.functional.Exceptions;
import org.clauseway.functional.Nothing;
import org.clauseway.logic.goals.optimizer.Barrier;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(access = AccessLevel.MODULE)
public class Conda implements Goal {
	List<Goal> clauses = new ArrayList<>();

	@Override
	public Conda orElse(Goal... goals) {
		if (goals.length == 0) {
			return this;
		}
		Conda next = new Conda();
		next.clauses.addAll(clauses);
		next.clauses.add(goals.length == 1 ?
				goals[0] :
				Conjunction.of(goals));
		return next;
	}

	/** The judge is a crossing: what the optimizer parked is discharged once, before any alternative is tried. */
	@Override
	public Cont<Knowledge, Nothing> apply(Knowledge entered) {
		return Barrier.settleAndThen(this::judge).apply(entered);
	}

	private Cont<Knowledge, Nothing> judge(Knowledge s) {
		return Cont.callCC(exit -> Cont.suspend(k -> {
			AtomicBoolean committed = new AtomicBoolean(false);
			List<Knowledge> results = new ArrayList<>();
			return clauses.stream()
					.reduce(Fiber.done(nothing()),
							(acc, g) -> acc.flatMap(_0 ->
									// an open read: the winner continues the branch, its parked
									// conditions ride — judging under an unripe one is the open
									// question of entailment-vs-satisfiability
									Exhaustion.exhausted(Barrier.of(g).apply(s).run(s1 -> {
										results.add(s1);
										return nothing();
									})).flatMap(_1 -> {
										if (committed.get() || results.isEmpty()) {
											return done(nothing());
										}
										committed.set(true);
										return results.stream()
												.map(exit::<Knowledge>with)
												.map(c -> c.runRec(k))
												.reduce(done(nothing()),
														(l, r) -> l.flatMap(_2 -> r));
									})),
							Exceptions.throwingBiOp(UnsupportedOperationException::new));
		}));
	}

	@Override
	public String toString() {
		return "(" + clauses.stream()
				.map(Objects::toString)
				.collect(Collectors.joining(" orElse ")) + ")";
	}
}
