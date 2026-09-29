package org.clauseway.logic.solving;

// ABOUTME: Pins the Query front door skeleton: fill-absent slot defaults on the
// ABOUTME: root, the occupied-slot refusal, and run/stream emitting one
// ABOUTME: Knowledge per derivation.

import org.clauseway.logic.Utils;
import static org.clauseway.logic.nogoods.Exclusion.exclude;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.clauseway.functional.fibers.schedulers.BreadthFirstScheduler;
import org.clauseway.functional.fibers.interpreter.ScopeProfiler;
import org.clauseway.functional.algebra.BoundedSemiring;
import org.clauseway.functional.algebra.Semiring;
import org.clauseway.functional.algebra.Semirings;
import org.clauseway.logic.debug.ProfilerStore;
import org.clauseway.logic.debug.Trace;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.optimizer.Optimizer;
import org.clauseway.logic.goals.optimizer.OptimizerStore;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.tabling.table.Table;
import org.clauseway.logic.weight.SemiringStore;
import org.clauseway.logic.unification.terms.Reified;
import org.clauseway.logic.unification.terms.Unifiable;
import org.junit.Test;

public class QueryTest {

	@Test
	public void theRootCarriesATableByDefault() {
		Knowledge root = Query.of(Goal.success()).root();
		assertThat(root.getStores().get(Table.class).isDefined()).isTrue();
	}

	@Test
	public void aSeededRootKeepsItsOwnTable() {
		Table shared = Table.empty();
		Knowledge seeded = Knowledge.empty().withStore(shared);
		Knowledge root = Query.of(Goal.success()).from(seeded).root();
		assertThat(root.getStores().get(Table.class).get()).isSameAs(shared);
	}

	@Test
	public void anUnseededFromStillGetsTheTableDefault() {
		Knowledge root = Query.of(Goal.success()).from(Knowledge.empty()).root();
		assertThat(root.getStores().get(Table.class).isDefined()).isTrue();
	}

	@Test
	public void theExplicitTableLandsOnAnEmptyRoot() {
		Table shared = Table.empty();
		Knowledge root = Query.of(Goal.success()).tabled(shared).root();
		assertThat(root.getStores().get(Table.class).get()).isSameAs(shared);
	}

	@Test
	public void anExplicitTableOnAnOccupiedSlotRefuses() {
		Knowledge seeded = Knowledge.empty().withStore(Table.empty());
		assertThatThrownBy(() -> Query.of(Goal.success()).from(seeded).tabled(Table.empty()).root())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("table");
	}

	@Test
	public void runEmitsOneKnowledgePerDerivation() {
		Unifiable<Integer> x = lvar();
		List<Knowledge> answers = Utils.collect(
				Query.of(x.unifies(1).or(x.unifies(2))).run());
		assertThat(answers).hasSize(2);
		assertThat(answers.stream()
				.map(k -> k.walk(x).get())
				.collect(Collectors.toList()))
				.containsExactlyInAnyOrder(1, 2);
	}

	@Test
	public void streamIsTheHarvestViewOfRun() {
		Unifiable<Integer> x = lvar();
		try (Stream<Knowledge> answers = Query.of(x.unifies(1).or(x.unifies(2))).stream()) {
			assertThat(answers.map(k -> k.walk(x).get())
					.collect(Collectors.toList()))
					.containsExactlyInAnyOrder(1, 2);
		}
	}

	@Test
	public void weightedSeedsTheRingAndItsTable() {
		Semiring<SemiringStore> counting = SemiringStore.product(Semirings.COUNTING);
		Knowledge root = Query.of(Goal.success()).weighted(counting).root();
		assertThat(root.getStores().get(SemiringStore.class).isDefined()).isTrue();
		// a plain ring cannot thread weights through tabled calls — its table refuses
		Table table = (Table) root.getStores().get(Table.class).get();
		assertThatThrownBy(table::assertTablingAllowed)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("solveBounded");
	}

	@Test
	public void aBoundedRingTablesStreaming() {
		BoundedSemiring<SemiringStore> product = SemiringStore.boundedProduct(Semirings.MIN_PLUS);
		Knowledge root = Query.of(Goal.success()).weighted(product).root();
		((Table) root.getStores().get(Table.class).get()).assertTablingAllowed();
		assertThat(root.getStores().get(SemiringStore.class).isDefined()).isTrue();
	}

	@Test
	public void weightedRefusesAnOccupiedTableSlot() {
		Semiring<SemiringStore> counting = SemiringStore.product(Semirings.COUNTING);
		assertThatThrownBy(() -> Query.of(Goal.success())
				.tabled(Table.empty()).weighted(counting).root())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("weighted");
	}

	@Test
	public void weightedRefusesARootAlreadyCarryingTheRing() {
		Semiring<SemiringStore> counting = SemiringStore.product(Semirings.COUNTING);
		Knowledge seeded = Knowledge.empty().withStore(counting.one());
		assertThatThrownBy(() -> Query.of(Goal.success()).from(seeded).weighted(counting).root())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("SemiringStore");
	}

	@Test
	public void aGenericSlotPlantsAnyStore() {
		ProfilerStore mine = ProfilerStore.of();
		Knowledge root = Query.of(Goal.success()).slot(mine).root();
		assertThat(root.getStores().get(ProfilerStore.class).get()).isSameAs(mine);
	}

	@Test
	public void aSlotMeetingItsOwnFamilyRefuses() {
		assertThatThrownBy(() -> Query.of(Goal.success())
				.slot(ProfilerStore.of()).slot(ProfilerStore.of()).root())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ProfilerStore");
	}

	@Test
	public void solveStreamsReifiedLikeTheGoalDoor() {
		Unifiable<Integer> x = lvar();
		Goal goal = x.unifies(1).or(x.unifies(1)).or(x.unifies(2));
		List<String> door = goal.solve(x).map(Object::toString).collect(Collectors.toList());
		try (Stream<Reified<Integer>> mine = Query.of(goal).solve(x)) {
			assertThat(mine.map(Object::toString).sorted().collect(Collectors.toList()))
					.isEqualTo(door.stream().sorted().collect(Collectors.toList()));
		}
	}

	@Test
	public void solveRendersResidualsLikeTheGoalDoor() {
		Unifiable<Integer> x = lvar();
		List<String> door = exclude(x.unifies(3)).solve(x)
				.map(Object::toString).collect(Collectors.toList());
		try (Stream<Reified<Integer>> mine = Query.of(exclude(x.unifies(3))).solve(x)) {
			assertThat(mine.map(Object::toString).collect(Collectors.toList()))
					.containsExactlyElementsOf(door);
		}
	}

	@Test
	public void tracedReportsPortsInTheTracerDoorsOrder() {
		Unifiable<Integer> x = lvar();
		List<String> door = new ArrayList<>();
		x.unifies(1).or(x.unifies(2)).named("g").solve(x, recorder(door)).count();

		List<String> mine = new ArrayList<>();
		try (Stream<Reified<Integer>> s = Query.of(x.unifies(1).or(x.unifies(2)).named("g"))
				.traced(recorder(mine)).solve(x)) {
			s.count();
		}
		assertThat(mine).isEqualTo(door);
		assertThat(door).isNotEmpty();
	}

	@Test
	public void profiledCountsStepsLikeTheProfilerDoor() {
		Unifiable<Integer> x = lvar();
		ScopeProfiler profiler = new ScopeProfiler();
		try (Stream<Reified<Integer>> s = Query.of(x.unifies(1).or(x.unifies(2)).named("g"))
				.profiled(profiler).solve(x)) {
			assertThat(s.count()).isEqualTo(2);
		}
		assertThat(profiler.counts()).isNotEmpty();
	}

	@Test
	public void optimizedRunsThePrePassAndKeepsTheTableDefault() {
		// the goal door forgot the table when seeding the optimizer store —
		// under slots, forgetting is unrepresentable
		Knowledge root = Query.of(Goal.success()).optimized(new Optimizer() { }).root();
		assertThat(root.getStores().get(Table.class).isDefined()).isTrue();
		assertThat(root.getStores().get(OptimizerStore.class).isDefined()).isTrue();

		Unifiable<Integer> x = lvar();
		try (Stream<Reified<Integer>> s = Query.of(x.unifies(1).or(x.unifies(2)))
				.optimized(new Optimizer() { }).solve(x)) {
			assertThat(s.count()).isEqualTo(2);
		}
	}

	private static Trace.Tracer recorder(List<String> ports) {
		return new Trace.Tracer() {
			@Override
			public void onCall(String label, Knowledge state) {
				ports.add("Call " + label);
			}

			@Override
			public void onExit(String label, Knowledge state) {
				ports.add("Exit " + label);
			}

			@Override
			public void onRedo(String label, Knowledge state) {
				ports.add("Redo " + label);
			}

			@Override
			public void onFail(String label, Knowledge state) {
				ports.add("Fail " + label);
			}
		};
	}

	@Test
	public void onInstallsTheChosenDriver() {
		AtomicBoolean used = new AtomicBoolean(false);
		Unifiable<Integer> x = lvar();
		try (Stream<Knowledge> answers = Query.of(x.unifies(1))
				.on(fiber -> {
					used.set(true);
					return new BreadthFirstScheduler<>(fiber);
				})
				.stream()) {
			assertThat(answers.count()).isEqualTo(1);
		}
		assertThat(used.get()).isTrue();
	}
}
