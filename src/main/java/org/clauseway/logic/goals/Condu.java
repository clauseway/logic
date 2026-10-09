package org.clauseway.logic.goals;

import org.clauseway.functional.Exceptions;
import org.clauseway.functional.Nothing;
import org.clauseway.logic.goals.optimizer.Barrier;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Cont;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(access = AccessLevel.MODULE)
public class Condu implements Goal {
	List<Goal> clauses = new ArrayList<>();

	@Override
	public Condu orElseFirst(Goal... goals) {
		if (goals.length == 0) {
			return this;
		}
		Condu next = new Condu();
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
			return clauses.stream()
					.reduce(
							Fiber.<Nothing> done(Nothing.nothing()),
							(acc, g) -> acc.flatMap(_0 -> {
								// DELIVERIES CROSS THE DELIMITER: collect the committed
								// solution inside the inner solve, hand it to the
								// continuation only after the seal - running k inside
								// would bill downstream work to the clause's workforce
								AtomicReference<Knowledge> won = new AtomicReference<>();
								// an open read: see Conda
								Fiber<Nothing> collected = Exhaustion.exhausted(Barrier.of(g).apply(s).runRec(s1 -> {
									if (committed.compareAndSet(false, true)) {
										won.set(s1);
									}
									return Fiber.done(Nothing.nothing()); // ignore subsequent solutions
								}));
								return collected.flatMap(_1 -> won.get() != null
										? exit.<Knowledge> with(won.get()).runRec(k)
										: Fiber.done(Nothing.nothing()));
							}),
							Exceptions.throwingBiOp(UnsupportedOperationException::new)
					);
		}));
	}

	@Override
	public String toString() {
		return "(" + clauses.stream()
				.map(Objects::toString)
				.collect(Collectors.joining(" orElseFirst ")) + ")";
	}
}
