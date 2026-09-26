package org.clauseway.logic;

// ABOUTME: The front door: seeds a root Knowledge through slots with
// ABOUTME: fill-absent defaults, runs a goal against it, and hands back the
// ABOUTME: knowledge it implies -- the Cont face primitive, Stream the harvest.

import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Scheduler;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.tabling.table.Table;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.Deque;
import java.util.Spliterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The front door of a solve: a goal, a root {@link Knowledge}, and the
 * slots that seed it. The result of solving is the knowledge the goal
 * implies — one {@link Knowledge} per derivation, duplicates and all (the
 * derivation view; folds are a downstream choice).
 *
 * <p>Slots hold one occupant and their defaults FILL ABSENT FAMILIES ONLY:
 * a root arriving through {@link #from} keeps every store it already
 * carries, and an explicit slot value meeting an occupied family refuses
 * loudly at build — forgetting a store and silently doubling one are both
 * unrepresentable.
 *
 * <p>{@link #run} is the primitive face: a {@link Cont} the caller applies
 * and drives with a scheduler of its own — engine-internal consumers stay
 * in the fiber world. {@link #stream} is the pull harvest for the world
 * boundary: lazy, one element per advance, close the stream to close the
 * driver.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Query {

	private final Goal goal;
	private final Knowledge from;
	private final Table table;
	private final Function<Fiber<Nothing>, Scheduler<Nothing>> driver;

	public static Query of(Goal goal) {
		return new Query(goal, null, null, null);
	}

	/** Root injection: start from existing knowledge instead of empty. */
	public Query from(Knowledge root) {
		return new Query(goal, root, table, driver);
	}

	/** The table slot — pass a shared table to join another solve's residence. */
	public Query tabled(Table table) {
		return new Query(goal, from, table, driver);
	}

	/** The driver slot; the default is a {@link BreadthFirstScheduler}. */
	public Query on(Function<Fiber<Nothing>, Scheduler<Nothing>> driver) {
		return new Query(goal, from, table, driver);
	}

	/** The seeded root, inspectable: slots checked and defaults filled here. */
	public Knowledge root() {
		Knowledge root = from != null ? from : Knowledge.empty();
		boolean occupied = root.getStores().containsKey(Table.class);
		if (table != null) {
			if (occupied) {
				throw new IllegalStateException(
						"the root already carries a table — an explicit tabled() may only"
								+ " fill an empty slot, never silently replace a residence");
			}
			return root.withStore(table);
		}
		return occupied ? root : root.withStore(Table.empty());
	}

	/** The primitive: one emission per derivation, driven by the caller. */
	public Cont<Knowledge, Nothing> run() {
		return goal.apply(root());
	}

	/** ask about {@code out}, answers tagged with the anchor itself. */
	public <T> Answers<Unifiable<T>, T> ask(Unifiable<T> out) {
		return ask(out, out);
	}

	/** ask about {@code out}, answers tagged {@code token} for routing. */
	public <R, T> Answers<R, T> ask(R token, Unifiable<T> out) {
		return new Answers<>(this, token, out);
	}

	/** The pull harvest of {@link #run}: lazy, closing closes the driver. */
	public Stream<Knowledge> stream() {
		return harvest(run(), factory());
	}

	/** The driver slot's occupant, defaulted. */
	Function<Fiber<Nothing>, Scheduler<Nothing>> factory() {
		return driver != null ? driver : BreadthFirstScheduler::new;
	}

	/** A Cont pulled as a lazy Stream: one element per advance, close closes the driver. */
	static <A> Stream<A> harvest(Cont<A, Nothing> source,
			Function<Fiber<Nothing>, Scheduler<Nothing>> factory) {
		Deque<A> results = new LinkedBlockingDeque<>();
		Fiber<Nothing> recur = source.run(v -> {
			results.add(v);
			return Nothing.nothing();
		});
		Scheduler<Nothing> scheduler = factory.apply(recur);

		Spliterator<A> spliterator = new Spliterator<A>() {
			@Override
			public boolean tryAdvance(Consumer<? super A> action) {
				while (results.isEmpty()) {
					if (scheduler.advance(v -> { })) {
						// driver done: whatever is buffered is all there will
						// ever be — hand it out one element per call below
						break;
					}
				}
				if (results.isEmpty()) {
					return false;
				}
				action.accept(results.poll());
				return true;
			}

			@Override
			public void forEachRemaining(Consumer<? super A> action) {
				// bulk delivery is legal HERE (unlike tryAdvance): drain the
				// buffer between driver batches without per-element dispatch
				while (true) {
					while (!results.isEmpty()) {
						action.accept(results.poll());
					}
					if (scheduler.advance(v -> { })) {
						while (!results.isEmpty()) {
							action.accept(results.poll());
						}
						return;
					}
				}
			}

			@Override
			public Spliterator<A> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics() {
				return Spliterator.ORDERED | Spliterator.NONNULL;
			}
		};

		return StreamSupport.stream(spliterator, false)
				.onClose(() -> {
					try {
						scheduler.close();
					} catch (Exception e) {
						throw new RuntimeException("Failed to close the driver", e);
					}
				});
	}
}
