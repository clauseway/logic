package org.clauseway.logic.debug;

import org.clauseway.logic.debug.Trace;
import org.clauseway.logic.solving.Query;
import org.clauseway.logic.TestSchedulers;
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.assertj.core.api.Assertions.assertThat;

import org.clauseway.logic.debug.Trace.Tracer;
import org.clauseway.logic.goals.Goal;
import org.clauseway.logic.goals.Knowledge;
import org.clauseway.logic.unification.terms.Unifiable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TraceTest {

	private static final class Recorder implements Tracer {
		final List<String> ports = new ArrayList<>();

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
	}

	@Test
	public void shouldReportCallAndOneExitPerSolution() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> x = lvar();

		Goal g = Trace.traced("g", x.unifies(1).or(x.unifies(2)), recorder);
		long count = Query.of(g).on(TestSchedulers.factory()).solve(x).count();

		assertThat(count).isEqualTo(2);
		assertThat(recorder.ports).containsExactly("Call g", "Exit g", "Redo g", "Exit g");
	}

	@Test
	public void shouldReportFailWhenExhaustedWithNoSolution() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> x = lvar();

		// contradictory: x cannot be both 1 and 2
		Goal g = Trace.traced("g", x.unifies(1).and(x.unifies(2)), recorder);
		long count = Query.of(g).on(TestSchedulers.factory()).solve(x).count();

		assertThat(count).isEqualTo(0);
		assertThat(recorder.ports).containsExactly("Call g", "Fail g");
	}

	@Test
	public void shouldAutoTraceNamedGoalsThroughTheStore() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();

		// no hand-wrapping: named goals report their ports because a tracer is seeded
		Goal inner = y.unifies(10).named("inner");
		Goal outer = x.unifies(1).and(inner).named("outer");
		Query.of(outer).traced(recorder).solve(x).count();

		// every named goal reached reports its ports; inner nests within outer
		assertThat(recorder.ports)
				.containsSubsequence("Call outer", "Call inner", "Exit inner", "Exit outer");
	}

	@Test
	public void shouldNotInterleaveSiblingBranchBodies() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> a = lvar();
		Unifiable<Integer> b = lvar();

		Goal left = a.unifies(1).and(b.unifies(10).named("p1")).named("LEFT");
		Goal right = a.unifies(2).and(b.unifies(20).named("q1")).named("RIGHT");
		Query.of(left.or(right).named("ROOT")).traced(recorder).solve(a).count();

		// the trace reads depth-first: LEFT's body finishes before RIGHT's body starts
		assertThat(recorder.ports.indexOf("Exit p1"))
				.isLessThan(recorder.ports.indexOf("Call q1"));
	}

	@Test
	public void shouldRenderPackageAwareLabelWithCurrentBindings() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> x = lvar();

		// the label walks x against the state, so it is rendered per port
		Goal g = x.unifies(5).named(pkg -> "x=" + pkg.walk(x));
		Query.of(g).traced(recorder).solve(x).count();

		// at Exit x is bound, so the label shows the value, not the variable name
		assertThat(recorder.ports.stream()
				.anyMatch(p -> p.startsWith("Exit x=") && p.contains("5")))
				.isTrue();
	}

	@Test
	public void traceSolvesAndPrints() {
		java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
		java.io.PrintStream original = System.out;
		System.setOut(new java.io.PrintStream(captured));
		long count;
		try {
			Unifiable<Integer> x = lvar();
			count = Query.of(x.unifies(1).named("g")).traced(Trace.printing()).solve(x).count();
		} finally {
			System.setOut(original);
		}

		assertThat(count).isEqualTo(1);
		assertThat(captured.toString()).contains("Call g").contains("Exit g");
	}

	@Test
	public void shouldFilterPortsByLabel() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> x = lvar();

		Goal keep = x.unifies(1).named("keep");
		Goal drop = Goal.success().named("drop");
		Query.of(keep.and(drop)).traced(recorder.filter(label -> label.contains("keep"))).solve(x).count();

		// only the matching box's ports reach the inner tracer
		assertThat(recorder.ports).containsExactly("Call keep", "Exit keep");
	}

	@Test
	public void shouldDeepenSpineForNestedGoals() {
		Map<String, Integer> depthAtCall = new HashMap<>();
		Tracer tracer = new Tracer() {
			@Override
			public void onCall(String label, Knowledge state) {
				depthAtCall.putIfAbsent(label,
						DebugStore.from(state).map(DebugStore::depth).getOrElse(0));
			}

			@Override
			public void onExit(String label, Knowledge state) {
			}

			@Override
			public void onRedo(String label, Knowledge state) {
			}

			@Override
			public void onFail(String label, Knowledge state) {
			}
		};
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();

		Goal inner = y.unifies(10).named("inner");
		Goal outer = x.unifies(1).and(inner).named("outer");
		Query.of(outer).traced(tracer).solve(x).count();

		assertThat(depthAtCall.get("inner")).isGreaterThan(depthAtCall.get("outer"));
	}

	@Test
	public void shouldNestPorts() {
		Recorder recorder = new Recorder();
		Unifiable<Integer> x = lvar();
		Unifiable<Integer> y = lvar();

		Goal inner = Trace.traced("inner", y.unifies(10), recorder);
		Goal outer = Trace.traced("outer", x.unifies(1).and(inner), recorder);
		Query.of(outer).on(TestSchedulers.factory()).solve(x).count();

		// outer Call, inner runs and exits, outer exits
		assertThat(recorder.ports).containsExactly("Call outer", "Call inner", "Exit inner", "Exit outer");
	}
}
