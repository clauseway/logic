// ABOUTME: Observation of a running solve: box-model tracing (Call/Exit/Redo/Fail ports)
// ABOUTME: and profiling, each carried through the Knowledge store map as a Packaged payload.

/**
 * Tracing and profiling of a solve. {@link org.clauseway.logic.debug.Trace} reports
 * the four box-model ports of a goal (Call, Exit, Redo, Fail) to a
 * {@link org.clauseway.logic.debug.Trace.Tracer}; {@code Trace.printing()},
 * {@code Trace.spy(names)} and {@code Trace.hiding(substrings)} build the usual
 * tracers. {@link org.clauseway.logic.debug.DebugStore} carries the active tracer and
 * the spine of open boxes through the {@link org.clauseway.logic.goals.Knowledge}
 * store map; {@link org.clauseway.logic.debug.ProfilerStore} marks a profiling solve
 * and labels each named goal's extent. Both are
 * {@link org.clauseway.logic.goals.Packaged} payloads that constraint processing
 * ignores.
 *
 * <p>They are planted at the root by {@code org.clauseway.logic.solving.Query}'s
 * {@code traced(tracer)} and {@code profiled(profiler)} slots, and read by
 * {@link org.clauseway.logic.goals.NamedGoal}, the hook that emits ports and extents
 * when a store is present and costs nothing otherwise.
 */
package org.clauseway.logic.debug;
