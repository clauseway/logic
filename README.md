# logic

A relational (logic) programming engine for Java 8 — miniKanren with
constraints, nogoods (negation as forbidden conjunctions), tabling,
semiring-weighted inference, self-planning queries, and pluggable, fair
search. Embeddable: your data stays Java objects, your queries are Java
expressions, and answers come back as a `java.util.stream.Stream`.

```java
Unifiable<LList<Integer>> xs = lvar(), ys = lvar(), zs = lvar();

// appendo is a RELATION, not a function — run it backwards:
// "which xs and ys concatenate to [1..6]?"
Query.of(Logic.appendo(xs, ys, zs)
                .and(zs.unifies(LList.ofAll(1, 2, 3, 4, 5, 6))))
        .solve(lval(Tuple.of(xs, ys)))
        .forEach(System.out::println);
// ((), (1,2,3,4,5,6)), ((1), (2,3,4,5,6)), ... all seven splits
```

This is a research/learning project (see [Status](#status)) — but a serious
one: the engine's guarantees are theorems of two algebras, and the test suite
checks the algebra's laws alongside the code.

## Why this engine

The combination is the point — these rarely live in one system:

- **One front door** — every solve is a `Query`: a goal plus composable
  capability slots (`tabled`, `weighted`, `traced`, `profiled`, `optimized`,
  `on` for the driver, `slot` for your own store). Slots fill only absent
  families and refuse conflicts loudly, so a traced weighted solve under a
  custom scheduler is one expression, and a misconfigured root is
  unrepresentable. Reading is a choice, not an accident: `solve(out)` streams
  classic reified answers; `select(vars).rows()` streams conditional rows.
- **Relational core** — unification over Java values, including tuples and
  collections structurally (`Tuple.of(x, 42)` unifies element-wise). Goals
  compose with `and`/`or`; relations run in any direction.
- **Constraint domains** — finite domains with bounds propagation to fixpoint
  (`dom`, `leq`, `addo`, `multo`, …) over typed families (`Longs`, `Ints`,
  `Dates`, `Instants`, `BigDecimals`), and projection (suspend a goal until a
  term is ground). Domains compose: mix FD, nogoods and plain unification in
  one query and the answers stay complete.
- **A nogood store** — negative knowledge as data: `exclude(literals...)`
  states one NOGOOD, "not all of these at once". One literal is classic
  disequality; several literals forbid a conjunction; and because the
  literals are ordinary postings, whole patterns — including calls into
  relations — can be negated. Nogoods propagate (a binding that would
  complete a forbidden conjunction fails the branch), survive into answers
  as explicit `¬(...)` conditions when undecided, and in the data layer
  compile to SQL (`NOT EXISTS`) for pushdown.
- **Tabling with full completion** — memoized relations. Left-recursive and
  mutually recursive rules terminate; the engine detects, per call, the moment
  no further answer can arrive (full SLG-style completion, including
  mutual-recursion rings), and completed calls become reusable data: a finished
  general call answers its instances without recomputation.
- **Tabling under constraints (TCLP)** — a tabled call under constraint
  knowledge is keyed by its REGION ("answers given `x ∈ {1..10}`"), and
  answers carry the conditions they are proven under — conditional answers,
  summed in a law-checked constraint ring where subsumption dedup is the
  ring's own absorption law. A ground answer streams the moment it is
  derived; a conditional one delivers final at completion. FD and nogood
  knowledge ride keys and answers alike, and a wider cached call serves
  narrower ones through the same ring.
- **Weighted inference** — attach a weight to any branch (`factor`) and the
  same program answers quantitative questions: how many solutions (counting),
  how likely (probability), cheapest path (min-plus), best derivation
  (Viterbi), and *which facts, combined how* (provenance — the answer's
  lineage as a regular expression). Rings compose in one product store, so one
  pass computes several at once. Two tabled strategies, chosen by the ring's
  TYPE: bounded semirings stream through the fixpoint (`solveBounded`); closed
  semirings defer to the seal, where **recursion is solved, not run** —
  a cyclic relation is read off as a linear equation system and its
  infinitely many derivations are summed in closed form by Kleene star
  (`solveClosed`). "Probability a retry loop ever succeeds" is one query,
  answered exactly — the geometric series, not a truncated simulation.
- **A self-planning optimizer** — every priceable goal declares the maximum
  number of answers it can emit; conjunctions sort cheapest-first around
  barriers, dead branches price to zero before they spawn, and a tabled call's
  price drops from unknown to exact the moment it completes. Clause order in
  the source stops mattering: the naive program is the fast program.
- **Fair, pluggable search** — breadth-first by default (complete: an answer at
  depth n is found even if another branch diverges), depth-first for
  Prolog-order traces, fork/join via `on(ForkJoinScheduler::new)`. Schedulers
  are drivers over one step interpreter; swapping them never changes the
  answer set, only the order.
- **Aggregation** — `findall`, `count`, `sum`, `max`, `min` reflect a
  sub-search into a value, folding through law-checked monoid witnesses.
- **A real debugger** — a Prolog box-model tracer (`Call`/`Exit`/`Redo`/`Fail`)
  with arguments rendered against the live state, and spypoints.

## The algebra is load-bearing

The engine's core claims are algebraic, and the code enforces them mechanically:

- Knowledge carriers (FD domains, nogood records, tabled answer sets) are
  declared **lattice instances**; goal pricing runs through a law-checked
  **semiring**; aggregation folds through **monoid witnesses**. The sibling
  `functional` library ships the interfaces and the law kits.
- The most legible instance: tabling's answer cell is a **constraint ring**
  (`Residues`/`Condition` — the c-table algebra). Its laws ARE the
  mechanics: subsumption dedup is the ring's absorption law
  (`a ∨ (a ∧ b) = a`), and "stream now vs deliver at completion" is
  boundedness (`1 ⊕ a = 1`: a value at the top can never change, so it is
  final on arrival). `docs/reference/condition.md` tells the story.
- A coverage gate fails the build if any algebraic implementor lacks a law
  test — claims are audited, not aspirational. Writing these laws found real
  bugs in mature code (an `X∩∅=X` in interval intersection among them).
- Optional semiring capabilities are **types**, not flags: `IdempotentSemiring`
  (the dedup license), `ClosedSemiring` (Kleene star — "no closure" is
  unrepresentable), `BoundedSemiring` (`a⊕1=1`, hence `a*=1` — the exact
  threshold at which streaming through a cycle terminates, and the type the
  streaming path demands), `SuperiorSemiring` (best-first commitment). Call
  sites that need a capability demand it in their signature — which is how
  the engine picks stream-vs-star per solve, visibly, at the call site. The
  same idea runs the front door: `weighted(ring)` picks the tabling mode by
  the ring's static type.
- The weighted witnesses are law-checked like everything else — including
  `Provenance`, the free closed semiring (regular expressions), whose star
  laws hold up to *language* equivalence; the law kit takes the equivalence
  as a parameter rather than pretending structural equality.
- Tabling's completion detection is Dijkstra–Scholten termination detection
  built from the same discipline: monotone counters, an upward-closed seal flag
  readable without locks, and a group-seal rule that is one lattice
  fixpoint. `docs/reference/table-completion.md` and
  `docs/reference/group-seal.md` tell that story end to end.

## Building

`logic` sits in the Clauseway family: [`functional`](../functional) beneath
it (continuations, fibers, schedulers, the algebra and its law kits),
[`pldb`](../pldb) beside it — the data boundary: relations as functions
over real backends (SQL with constraint and nogood pushdown, a coverage
cache, and a transactional write face) — and [`apps/library`](../apps/library)
as the worked example whose friction ledger drives the design. All are Maven
projects, Java 8, Apache-2.0, currently `-SNAPSHOT`. vavr is consumed as a
relocated artifact (`clauseway-vavr`, built once from `logic/vavr/`):

```bash
cd functional && mvn install
cd ../logic   && mvn -f vavr/pom.xml install && mvn install
```

```xml
<dependency>
    <groupId>org.clauseway</groupId>
    <artifactId>logic</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## A tour

### Relations and unification

```java
import static org.clauseway.logic.unification.terms.LVar.lvar;
import static org.clauseway.logic.unification.terms.LVal.lval;
import org.clauseway.logic.solving.Query;

Unifiable<String> who = lvar();
Query.of(who.unifies("world"))
        .solve(who)                    // Stream<Reified<String>>
        .forEach(System.out::println); // {world}
```

A `Goal` is a value; build them with `and`, `or`, `Goal.defer` (for recursion),
`Logic.exist` (fresh variables), and the pattern-matching sugar in `Matche`.
A `Query` is the one door: configure with slots, then read — `solve(out)` for
classic reified answers, `select(vars...).rows()` for conditional rows, `run()`
for the raw solver states if you are building machinery.

### Nogoods — negation as data

```java
import static org.clauseway.logic.nogoods.Exclusion.exclude;

Unifiable<Integer> x = lvar();
Query.of(Logic.membero(x, lval(LList.ofAll(1, 2, 3)))
                .and(exclude(x.unifies(2))))       // one literal = disequality
        .solve(x);                                 // {1}, {3}

// several literals forbid the CONJUNCTION — x=3 ∧ y=4 jointly outlawed:
exclude(x.unifies(3), y.unifies(4))
```

A nogood propagates: the binding that would complete a forbidden conjunction
fails its branch on the spot. A nogood the solve cannot decide survives into
the answer as an explicit residual — `_.0 : ¬(_.0 ≡ {3})` — rather than
being dropped. Because literals are ordinary postings, `exclude` scales from
disequality up to negating whole patterns; the data layer
([`pldb`](../pldb)) negates *derived relations* by sealing their extension
and posting it as nogoods, and compiles nogoods to `NOT EXISTS` for SQL
pushdown.

### Finite domains

```java
Unifiable<Long> a = lvar(), b = lvar(), sum = lvar();

FiniteDomain.dom(a, Longs.range(0L, 10L))                  // a ∈ {0..9}
        .and(FiniteDomain.dom(b, Longs.range(0L, 10L)))
        .and(Longs.addo(a, b, sum))                        // a + b = sum
        .and(sum.unifies(10L))
        .and(Longs.lss(a, b));                             // a < b
Query.of(...).select(a, b).rows();
// (1,9), (2,8), (3,7), (4,6)
```

Constraints propagate as bounds narrow — `x≤y≤z` chains prune before
labelling, not during generate-and-test. Domains and nogoods cooperate:
`x ∈ {4,5} ∧ x ≠ 5` yields exactly `4`. Arithmetic and order come per typed
family — `Longs`, `Ints`, `Dates`, `Instants`, `BigDecimals`.

### Conditional rows

```java
Unifiable<String> p = lvar(), c = lvar();
Query.of(parent(p, c))
        .select(p, c)            // the projection; reading still open
        .rows()                  // distinct by default; .all() for the bag,
        .forEach(row -> {        // .raw() for regions instead of labelling
            Reified<String> who = row.get(p);   // typed by the key
            row.getCondition();                 // what this row holds under
        });
```

`select` enforces like a classic solve (domains label into ground rows), and
what a store could not decide rides the row as its `Condition` instead of
being silently dropped.

### Tabling

```java
Tabled<Tuple2<Unifiable<String>, Unifiable<String>>> ancestor =
        Tabling.define(args -> args.apply((x, y) ->
                parent(x, y)
                        .or(defer(() -> {
                            Unifiable<String> z = lvar();
                            return parent(x, z).and(ancestor.apply(Tuple.of(z, y)));
                        }))));

Query.of(x.unifies("alice").and(ancestor.apply(Tuple.of(x, y))))
        .solve(y);                     // bob, charlie, david — and it TERMINATES
```

Each distinct call pattern gets its own answer table; the engine detects
per-call completion mid-solve (mutual recursion included), after which the
call prices exactly, reorders freely, and serves more-specific calls from its
cache — `ancestor("alice", Y)` completed means `ancestor("alice", "david")`
is a lookup, not a search.

### Weighted answers

```java
// two routes A→D; ask for count and cost IN ONE PASS
Goal viaB = mid.unifies("B").and(factor(MIN_PLUS, 1L)).and(factor(MIN_PLUS, 5L));
Goal viaC = mid.unifies("C").and(factor(MIN_PLUS, 2L)).and(factor(MIN_PLUS, 2L));

SemiringStore total = Weights.solve(viaB.or(viaC),
        SemiringStore.product(COUNTING, MIN_PLUS), BreadthFirstScheduler::new);
total.get(COUNTING);   // 2 routes
total.get(MIN_PLUS);   // 4 — the cheaper one
```

`factor` multiplies a weight along a derivation; `⊕` combines rival
derivations; the ring decides what those mean. Through a *tabled* recursion the
ring's type picks the strategy: a `BoundedSemiring` streams
(`solveBounded` — min-plus shortest paths over cyclic graphs), a
`ClosedSemiring` waits for completion and solves the recursion as equations
(`solveClosed`):

```java
// retry loop: succeed now (1/6) or pay a step (5/6) and loop.
// P(ever succeeds)? The star sums the geometric series: exactly 1.0.
// PROB is user-defined — probability (+,×) with star a* = 1/(1−a); it is
// kept out of Semirings deliberately (⊕-as-probability is only sound over
// DISJOINT derivations, and that is the user's claim to make)
Tabled<Tuple1<Unifiable<Integer>>> ever = Tabling.defineRecursive(self -> t ->
        t.apply(x -> x.unifies(1).and(factor(PROB, 1.0 / 6))
                .or(x.unifies(1).and(factor(PROB, 5.0 / 6))
                        .and(defer(() -> self.apply(t))))));

Weights.solveClosed(ever.apply(Tuple.of(lval(1))), out,
        SemiringStore.closedProduct(PROB), BreadthFirstScheduler::new);
// one answer, weight 1.0 — infinitely many derivations, summed in closed form
```

Under `PROVENANCE` the same query returns the *shape* of all those derivations
as a finite regular expression (`step*·base`) — an executable audit trail for a
recursive answer. Mutual recursion works (the coupled calls are solved as one
matrix); nonlinear recursion (two recursive calls in one clause) is refused
loudly — star closes linear systems only.

### The optimizer, the tracer, the profiler — slots

```java
// same answers regardless of clause order — the pass sorts cheapest-first
Query.of(goal).optimized(new OrderingOptimizer()).solve(out);

// Prolog-order box-model trace; spypoints filter
Query.of(goal).traced(Trace.spy("appendo")).solve(out);

// slots COMPOSE — the old fused entry points could not:
Query.of(goal).optimized(planner).profiled(profiler).on(factory).solve(out);
```

```
Call: (1,2,3) ++ <_.1> ≣ (1,2,3,4,5,6)
 Call: (2,3) ++ <_.1> ≣ (2,3,4,5,6)
  ...
 Exit: (2,3) ++ (4,5,6) ≣ (2,3,4,5,6)
Exit: (1,2,3) ++ (4,5,6) ≣ (1,2,3,4,5,6)
```

The optimizer rides the solver state: freshly unfolded recursion layers are
re-planned against live bindings, a `dom`-post over an already-disjoint domain
prices to zero (killing its branch before it spawns), and completed tabled
calls price at their exact answer count.

### Aggregation and projection

```java
Aggregate.count(x -> Logic.membero(x, lval(LList.ofAll(1, 2, 3))), n);   // n = 3

// suspend until x is ground, then compute with the actual value
Projection.project(x, v -> y.unifies(v * 2));
```

## What it's good at

Embedded logic inside JVM systems: test-data generation (write the invariant as a
relation, run it backwards, enumerate fairly), configurators and rule engines
(valid-combination problems with recursive rules and exclusions), deductive/
Datalog-style queries over in-memory data — negation included, type checkers
and program analyses for DSLs, puzzle-class constraint search and procedural
generation. The weighted layer adds the algebraic-path-problem family over the
same programs — shortest/most-reliable/bottleneck routes, route counting,
Markov absorption probabilities, lineage audits of recursive answers — one
relation text, many rings.

Scale honestly: bounds-consistency FD over tens-to-hundreds of variables, search
spaces that fit propagation-then-label — decision support, not an industrial CP
solver (no global constraints yet; the extension point below is where they'd go).

## Architecture, briefly

- A **goal** is `Knowledge -> Cont<Knowledge, Nothing>` (CPS). Success calls
  the continuation; failure stays silent.
- A **`Knowledge`** is the immutable solver state: substitutions + constraint
  stores. Backtracking is free — each branch keeps its own.
- **The front door** (`solving/`) is `Query`: capability slots seed the root
  (defaults fill absent families only; conflicts refuse), `run()` is the one
  primitive every reading consumes, and the readings are explicit — classic
  reified terms, or conditional rows with the extraction (enforce vs raw
  regions) and multiplicity (distinct vs all) as user switches. `Goal` itself
  has no solve methods. `docs/reference/solving.md` is the contract.
- **Search** is a set of scheduler drivers over one step interpreter (in
  `functional`); breadth-first is the default, and tracing defaults to
  depth-first so traces read in Prolog order.
- **Constraints** follow a capability design: a store is a `Theory` (its
  knowledge, an atom set in normal form) paired with a `Factor` (its
  behavior); the driver (`constraints/Propagation`) speaks through two
  normalize triggers — bindings arrived, or knowledge arrived at a door —
  each answered by a `Fiber<Revision>`. A store can swap only its own
  entry, and the breaking actions (touching the substitution, another
  store's state, forgetting to re-park a constraint) are unrepresentable
  by type. The two shipped value families are the lattice store (finite
  domains) and the **nogood store** (`NogoodConstraints`: records are
  forbidden conjunctions, verified by re-imposing them on a scratch world —
  fail = refuted, unchanged = crossed off, new knowledge = still owed).
  New constraint domains implement one interface.
- **Tabling** rides the fiber substrate's two primitives (in `functional`):
  a `Scope`, whose monotone counters detect quiescence — the seal — and a
  `Channel`, a monotone value that grows and wakes parked consumers. The
  answer cell is one `JoinMap` from reified answer terms to values in the
  mode's semiring — a `Condition` (the region an answer is proven on) for
  plain and TCLP tabling, the weight ring for weighted — with an ascent log
  both reader kinds cursor. Delivery timing is the values' own finality: a
  value at ⊕'s top streams on arrival, anything below waits for the seal.
  A tabled body runs as an **anonymous master** (detached work billed to
  its own call) and every caller reads through a consumer, so entries seal
  in dependency order.
  The concurrency contract — the lock graph and the three invariants that
  keep completion sound under the fork/join scheduler — is written down in
  `table-completion.md` §6a and guarded by a dedicated parallel stress test.
- **Weighted tabling** is a strategy seam (`TablingMode`) on that skeleton:
  `Streaming` folds values through the fixpoint; the weight package's
  `Closed` mode explores structure only, records each derivation as a base
  or an edge of a first-class equation graph, and at each seal runs
  Floyd–Warshall/Kleene over the sealed closure and replays the reader
  chains with the solved values.

The design record lives in `docs/` — `vision.md` (the north star and
roadmap) and `method.md` (how the design process works) at the top,
`reference/` for the as-built theory, `design/` for approved-but-unbuilt
work, `shelved/` for sketches waiting on their triggers. Start with
`reference/lattice.md` (the engine's one algebra and its quotient tower),
then `condition.md` (the constraint ring: answers as semiring values),
`solving.md` (the front door), `constraint-kernel.md` (the constraint
engine as shipped), `design/nogood-store.md` (negative knowledge),
`table-completion.md` (tabling's completion machinery), and
`star-tabling.md` (closed-semiring tabling: why streaming diverges, how
star closes it). `CLAUDE.md` carries the working-on-this map: landmines,
seams, backlog.

## Status

A research/learning project, built with viability as a constraint rather than a
goal: the designs are the kind that could be real (honest concurrency, measured
claims, no toy shortcuts), but there is no release and APIs move freely.
Java 8, Apache-2.0, no runtime dependencies beyond the relocated vavr
(`clauseway-vavr`) and the sibling `functional` library. ~870 tests, including
law suites for every declared algebraic instance and a parallel stress test on
tabling's completion machinery. If you're reading this as a source of ideas
rather than a dependency, `docs/` is the interesting part.
