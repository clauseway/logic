package org.clauseway.logic.solving;

// ABOUTME: The front door: seeds a root Knowledge through slots with
// ABOUTME: fill-absent defaults, runs a goal against it, and hands back the
// ABOUTME: knowledge it implies -- the Cont face primitive, Stream the harvest.

import java.util.stream.Collectors;
import org.clauseway.functional.Exceptions;
import org.clauseway.functional.algebra.BoundedSemiring;
import org.clauseway.functional.algebra.ClosedSemiring;
import org.clauseway.functional.algebra.Semiring;
import org.clauseway.functional.Nothing;
import org.clauseway.functional.fibers.Cont;
import org.clauseway.functional.fibers.Fiber;
import org.clauseway.functional.fibers.Scheduler;
import org.clauseway.functional.fibers.interpreter.ScopeProfiler;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.functional.fibers.schedulers.DepthFirstScheduler;
import org.clauseway.logic.constraints.Constraints;
import org.clauseway.logic.constraints.Propagation;
import org.clauseway.logic.debug.DebugStore;
import org.clauseway.logic.debug.ProfilerStore;
import org.clauseway.logic.debug.Trace;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Packaged;
import org.clauseway.logic.goals.optimizer.Optimizer;
import org.clauseway.logic.goals.optimizer.OptimizerStore;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.tabling.table.Table;
import org.clauseway.logic.weight.SemiringStore;
import org.clauseway.vavr.collection.List;
import java.util.Arrays;
import org.clauseway.logic.unification.terms.Reified;
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
import lombok.Value;
import lombok.With;

/**
 * The entry point of a solve: a goal, a root {@link Knowledge}, and the
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
 * <p>{@link #run} is the primitive: a {@link Cont} the caller applies
 * and drives with a scheduler of its own — engine-internal consumers stay
 * in the fiber world. {@link #stream} is the pull harvest for the world
 * boundary: lazy, one element per advance, close the stream to close the
 * driver.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@With(AccessLevel.PRIVATE)
public final class Query {

	private final Goal goal;
	private final Knowledge from;
	private final Table table;
	private final Table ringTable;
	private final Function<Fiber<Nothing>, Scheduler<Nothing>> driver;
	private final ScopeProfiler profiler;
	private final Optimizer optimizer;
	private final List<Slot> slots;

	/** One store to plant at the root, remembering which slot method injected it. */
	@Value
	private static class Slot {
		Packaged store;
		String owner;
	}

	public static Query of(Goal goal) {
		return new Query(goal, null, null, null, null, null, null, List.empty());
	}

	/** Root injection: start from existing knowledge instead of empty. */
	public Query from(Knowledge root) {
		return withFrom(root);
	}

	/** The table slot — pass a shared table to join another solve's residence. */
	public Query tabled(Table table) {
		return withTable(table);
	}

	/** The driver slot; the default is a {@link BreadthFirstScheduler}. */
	public Query on(Function<Fiber<Nothing>, Scheduler<Nothing>> driver) {
		return withDriver(driver);
	}

	/**
	 * The generic slot: plant any store at the root. Fill-absent like every
	 * named slot — a slot meeting its own family (in the root or in another
	 * slot) refuses at build.
	 */
	public Query slot(Packaged store) {
		return slotted(store, "slot()");
	}

	/**
	 * The trace slot: seeds a {@link DebugStore} so every named goal reports
	 * its Call/Exit/Redo/Fail ports, and fills an UNSET driver slot with
	 * depth-first — a branch runs to completion before its siblings, so the
	 * trace reads in Prolog order. An explicit {@link #on} wins; expect an
	 * interleaved trace under a concurrent driver.
	 */
	public Query traced(Trace.Tracer tracer) {
		return slotted(DebugStore.of(tracer), "traced()");
	}

	/**
	 * The profiler slot: seeds a {@link ProfilerStore} so every named goal
	 * names its dynamic extent, and installs the profiler as the driver's
	 * step listener — steps bill to goal names, workforce labels, or root.
	 */
	public Query profiled(ScopeProfiler profiler) {
		return withProfiler(profiler).slotted(ProfilerStore.of(), "profiled()");
	}

	/**
	 * The optimizer slot: the root tree is rewritten once against the
	 * initial substitution (the static tier), and an {@link OptimizerStore}
	 * is planted at the root so each recursion layer is rewritten as it unfolds at
	 * the {@code defer} hook.
	 */
	public Query optimized(Optimizer optimizer) {
		return withOptimizer(optimizer).slotted(OptimizerStore.of(optimizer), "optimized()");
	}

	/**
	 * The ring slot: weighs the solve under {@code ring} — plants the ring's
	 * {@code one()} as the running {@link SemiringStore} and COMPUTES the
	 * table slot from the ring. A plain semiring cannot thread weights
	 * through tabled calls, so its table refuses tabling; see the bounded
	 * and closed overloads for the tabling-capable rings.
	 */
	public Query weighted(Semiring<SemiringStore> ring) {
		return weighted(SemiringStore.table(ring), ring.one());
	}

	/** {@link #weighted(Semiring)} with streaming tabling: cells fold by the bounded ring. */
	public Query weighted(BoundedSemiring<SemiringStore> ring) {
		return weighted(SemiringStore.table(ring), ring.one());
	}

	/** {@link #weighted(Semiring)} with star tabling: values summed per sealed closure. */
	public Query weighted(ClosedSemiring<SemiringStore> ring) {
		return weighted(SemiringStore.table(ring), ring.one());
	}

	private Query weighted(Table ringTable, SemiringStore one) {
		return withRingTable(ringTable).slotted(one, "weighted()");
	}

	private Query slotted(Packaged store, String owner) {
		return withSlots(slots.append(new Slot(store, owner)));
	}

	/** The seeded root, inspectable: slots checked and defaults filled here. */
	public Knowledge root() {
		if (table != null && ringTable != null) {
			throw new IllegalStateException(
					"weighted() computes the table slot from its ring — it cannot"
							+ " combine with an explicit tabled()");
		}
		Knowledge root = from != null ? from : Knowledge.empty();
		Table wanted = table != null ? table : ringTable;
		boolean occupied = root.getStores().containsKey(Table.class);
		if (wanted != null) {
			if (occupied) {
				throw new IllegalStateException(
						"the root already carries a table — an explicit table slot may only"
								+ " fill an empty one, never silently replace a residence"
								+ (ringTable != null ? " (weighted() claims the slot for its ring)" : ""));
			}
			root = root.withStore(wanted);
		} else if (!occupied) {
			root = root.withStore(Table.empty());
		}
		return slots.toJavaStream()
				.reduce(root,
						(r, s) -> planted(r, s.getStore(), s.getOwner()),
						Exceptions.throwingBiOp(UnsupportedOperationException::new));
	}

	private static Knowledge planted(Knowledge root, Packaged store, String owner) {
		if (root.getStores().containsKey(store.getClass())) {
			throw new IllegalStateException("the root already carries a "
					+ store.getClass().getSimpleName() + " — " + owner
					+ " may only fill an empty slot");
		}
		return root.withStore(store);
	}

	/**
	 * The primitive: one emission per derivation, driven by the caller. Each
	 * emission is settled ({@link Propagation#settle}): no pending search rides
	 * a raw package.
	 */
	public Cont<Knowledge, Nothing> run() {
		Goal entry = optimizer == null
				? goal
				: new BreadthFirstScheduler<>(goal.accept(optimizer)).get();
		return Propagation.settled(entry.apply(root()));
	}

	/**
	 * The question's projection: which variables the rows report. Returns a
	 * {@link Selection} with the reading still open — extraction and
	 * multiplicity are chosen there before {@link Selection#rows()} streams.
	 */
	public Selection select(Unifiable<?> key, Unifiable<?>... keys) {
		return new Selection(this,
				Stream.concat(Stream.of(key), Arrays.stream(keys))
						.collect(Collectors.toList()),
				false, false);
	}

	/**
	 * The classic reading: answers as reified terms — stores enforce, the
	 * surviving residues render INTO the term ({@code Constrained}) — one
	 * element per derivation, streamed lazily under the query's driver.
	 */
	public <T> Stream<Reified<T>> solve(Unifiable<T> out) {
		return harvest(run().flatMap(s -> Constraints.reify(s, out)), factory());
	}

	/** The pull harvest of {@link #run}: lazy, closing closes the driver. */
	public Stream<Knowledge> stream() {
		return harvest(run(), factory());
	}

	/** The driver slot's occupant, defaulted; traced fills an unset slot with depth-first. */
	public Function<Fiber<Nothing>, Scheduler<Nothing>> factory() {
		Function<Fiber<Nothing>, Scheduler<Nothing>> base = driver != null
				? driver
				: slots.exists(slot -> slot.getStore() instanceof DebugStore)
						? DepthFirstScheduler::of
						: BreadthFirstScheduler::new;
		return profiler == null ? base : fiber -> base.apply(fiber).withListener(profiler);
	}

	/** A Cont pulled as a lazy Stream: one element per advance, close closes the driver. */
	public static <A> Stream<A> harvest(Cont<A, Nothing> source,
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
