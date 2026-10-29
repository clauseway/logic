# The solving front door — Query, Selection, Row, Selector

**Status: AS BUILT** (September 2026, the solving arc; §§4–5 amended October 2026 for the mapped reading). Authoritative for
the `solving` package's faces; read alongside `condition.md` (the guard
algebra) and `constraint-kernel.md` (what enforce and the stores do).

---

## 1. The artifact and the pipeline

The result of solving a goal is the knowledge it implies. `Knowledge`
(formerly `Package`) is that artifact: immutable substitutions plus
stores, one per derivation. Everything in `solving` is a way to configure
how that knowledge is derived or to read something out of it:

```
Query          configure: the goal + the slots that seed the root
  .run()       derive:    Cont<Knowledge, Nothing> — the primitive
  .stream()               lazy Stream of worlds — the pull harvest
  .solve(out)  read:      classic — Reified per derivation, residues
                          rendered into the term (Constrained)
  .select(vars...)        the selection: Selection<Row> → stream() → Row
  .select(selector)         the mapped reading: Selection<D> → stream() → D,
                            or rows() → Row, the same selection unmapped
```

`run()` is the one primitive; every other face is a consumer of it. It is
scheduler-free and push/pull-agnostic: the pull faces harvest it through a
driver, the produce seam applies it with an emitting continuation. There
is no separate produce face because none is needed.

## 2. Query and the slot discipline

Slots: `from` (root injection), `tabled`, `on` (driver), `weighted`,
`traced`, `profiled`, `optimized`, and the open door `slot(Packaged)`.

**The law: defaults fill absent families only.** A root arriving through
`from` keeps every store it carries; a default never overwrites; an
explicit slot value meeting an occupied family refuses loudly at build.
Forgetting a store and silently doubling one are both unrepresentable —
the bug class the old optimizer door shipped (a root built without a
table) cannot be written.

**Open values, closed slots.** Every slot accepts any value of its kind —
any `Scheduler` factory, any `Semiring`, any `Table`, and through
`slot(Packaged)` any store at all, riding the root with the same dignity
as the builtins. The set of *semantic* slots is closed on purpose: every
one that exists required kernel work (tabling modes, the `NamedGoal`
hooks, the `defer` rewrite hook), and no builder API could deliver a new
one. A named capability is a convenience that delegates into the same
slot list the open door uses.

**Driver resolution.** `on()` wins if set; otherwise depth-first when a
`DebugStore` rides (so a trace reads in Prolog order), else breadth-first.
`profiled` decorates whichever driver resolves with its step listener —
it composes with `on()`, which the old fused door could not.

**The weighted slot computes the table.** The ring's static type picks
the tabling mode — plain `Semiring` → a refusing table (weights cannot
thread through tabled calls), `BoundedSemiring` → streaming weighted
cells, `ClosedSemiring` → star tabling (`SemiringStore.table`). The
compatibility matrix is overload resolution; the conflicts (`weighted` ×
`tabled`, `weighted` × a root already carrying the ring) fall out of the
slot law with no extra machinery.

`root()` is inspectable — slots check and fill there. `run()` applies the
optimizer pre-pass (the static-tier rewrite) before entering the goal.
`harvest` is the one Cont→Stream machinery: lazy, one element per
`tryAdvance`, closing the stream closes the driver.

## 3. The two extractions

Reading a value out of an answer world is not a getter; there are exactly
two honest ways, and which one a face uses is its semantics:

- **`Constraints.enforced(world, anchor)`** — the commit stage of
  reification, alone: every store enforces its constraints about the
  anchor (labelling FORKS the answer, verdicts fail branches), then the
  answer may not leave while suspensions pend. What survives is the
  answer's honest residue. `reify` is this stage plus rendering the
  residue into the term; `select`'s default reading is this stage plus
  capture.
- **`Answer.capture(token, world, anchor)`** — RAW: the anchor's walked
  image plus the residual knowledge conditioning it (`Residues.all`),
  arriving as a one-region `Condition`. No enforcement runs. This is the
  produce seam's operation — a cache or a data plane wants the REGION a
  derivation denotes, labelling deferred to consumption — and it is why
  tabling and pldb's producers never called `reify`. It refuses under
  pending suspensions for tabling's exact reason: the owed condition
  cannot ride the answer.

A user-facing read must enforce first or wide answers smuggle
un-enforced knowledge into their conditions (the `dom` labelling oracle:
`select(x)` over `dom(x, 1..3)` yields three ground rows, never one wide
row with the domain in the condition).

## 4. The readings — Selection

`select(vars...)` names the projection and returns a `Selection<Row>`
with the reading still open, because different consumers want different
answers from the same question:

- **extraction** — enforced (default) or `raw()` (regions as derived).
- **multiplicity** — distinct (default: alpha-equal images fold in a
  `JoinMap`, conditions join by ⊕, absorption prunes) or `all()` (one row
  per derivation).

The distinct reading needs exhaustion, so its stream computes at the
`stream()` call; `all()` stays lazy. Folding by default loses nothing: the
derivation view survives whole at `run()`.

`Selection<D>` is typed by what it streams. `select(vars...)` streams the
solution itself; `select(selector)` streams the value a `Selector<D>` builds
from each solution (§5). The two switches are the same on both and keep
the type; `stream()` delivers, and `rows()` delivers the rows unmapped,
for a reading the selector's is not — a sort key, a fact, a second value
off the same keys — so none of those needs a door of its own.

## 5. Row, and the mapped reading

One solution's cells, read by the variable itself:

```java
<T> T get(Unifiable<T> key)            // the value, or a refusal: the gate into Java
<T> Reified<T> cell(Unifiable<T> key)  // the cell as the reifier states it
Row ground()                           // every cell ground, or a refusal
```

`get` is the reading into Java. A Java value has no place for a slot
name or a residual, so a cell that is not ground refuses, by column and
with the row rendered whole. `cell` comes back in output vocabulary,
stated as the reifier would state it — a ground cell is a value; a cell
the solution did not decide is its slot name (`Any`), or that name under
the answer's residuals about it (`Constrained`) when the answer is one
region, so a coupling between cells stays visible and is never smoothed
into an empty. A row folded from several regions shows the name alone;
the regions are `getCondition()`. An unselected key refuses at either.
`getCondition()` is the knowledge the solution holds under; `answer()`
is the whole seam artifact for anything that ships rows onward.

A solution renders whole, shaped as the select named it: every cell
under its name in the select's order, then the residuals when there are
any — `(x: {1}, y: _.0) : ¬(_.0 ≡ {2})`, regions ∨-joined, each region's
atoms ∧-joined. A wide row is still an answer, a symbolic one over
many ground results; `get` refuses with that rendering, and `ground()`
is the same check over every cell at once, naming the cells that are
not ground first, for a reading that wants the whole row or nothing.

A `Selector<D>` is a Java value's two faces at the boundary:

```java
Iterable<? extends Unifiable<?>> keys();   // the variables it is built from
Iterable<String> names();                  // what a solution shows the keys by; the keys unless overridden
D map(Row row);                        // the value one row builds
```

The engine is honest about its two directions — a goal takes
`Unifiable`, an answer is `Reified` — and Java has no such distinction,
so the merge is the variable itself: the same key enters the goal and
reads the answer, and the `Row` in between is the keyed `Unifiable` to
`Reified` mapping, the one place the engine's half ends and the user's
begins. `Query.select(selector)` selects the keys and streams `map` over
every solution; `map` reads through `get`, so a cell that is not ground
refuses where the mapper touches it, the solution rendered whole with
each cell under the name the selector gives its key. The solutions
reading is the identity mapping, so there is one pipeline, not two, and
`rows()` is that pipeline with the map left off.

That is the difference between the two tiers a user type can sign. A
`Structural` is rebuilt by the reifier, with whatever the answer holds —
a value, an `Any`, a `Constrained` — so its members must be `Term`, and
it is a term: it nests, unifies and travels in a list. A Selector's
value is built by the user's `map`, through `get`, which refuses
anything that is not a value, so its keys are `Unifiable` and the value
has ordinary Java fields. The map is the half the engine cannot write:
it does not know the target and could not fill it if it did.

pldb's `Schema<D>` is a Selector whose names are its columns, and
that is the whole of the Java interop story: a row type is variables in,
value out, and the engine never rebuilds it — which is why a Schema is
not a `Structural`. A user type that is a term, travelling inside a
list or unified as a whole, signs the structural contract instead, with
`Term` members and `Term.unifiable()` as the door into a goal.

## 6. Answer and Call, the duals

```
Call<R>    (token, args image, Residues)    — asks under ONE region
Answer<R>  (token, image,     Condition)    — holds under a DNF of regions
```

A call names the single region it asks from (its `Residues` conjunct);
an answer may hold under a disjunction, grown by ⊕ as derivations
arrive. The algebra's two operations split between them: `Call.subsumes`
is region containment, answers fold. The token slot `R` is any
value-keyed identity, and it doubles as the recovery schema: pldb's rows
are `Answer<Relation>` read through `Answers.get(answer, property)`; a
query-tier answer defaults its token to the anchor. `unconditional()` is
the one explicit guard-dropping bridge.

## 7. Who rides the door

- **Users** — every former `Goal.solve` variant is a slot spelling:
  `Query.of(g).traced(t).solve(x)`, `Query.of(g).on(f).solve(x)`, and
  combinations the fused doors could not express.
- **Weights** — thin doors over the Query faces; the ⊕-total is plain
  user code (worlds → stores → reduce). The per-answer pairing stays in
  `Weights` because a weight is a STORE-resident payload where a
  condition is answer-resident; it rides `run()`, not `Selection`.
- **pldb** — `Question` and `GoalProducer` build their roots by slots
  (`tabled(residence)`; the table default) and extract by `capture`; a
  pldb select inherits tracing, profiling, and shared tables for free.
- **Tabling does NOT.** Engine internals never import upward: tabling's
  body root is DERIVED state (`bodyState` + strip), not configuration,
  and routing it through Query would add a cycle and no semantics.
  Tabling consumes the solving VOCABULARY — `Call`, `Answer`,
  `Condition`, `Residues`, `JoinMap`, `capture` — never the faces.

The package holds two strata knowingly: the vocabulary (below tabling —
it never imports upward) and the faces (`Query`/`Selection`/`Row`/`Selector`,
above everything — they import tabling and weight). The package-granularity
cycle with tabling and weight is accepted; the discipline lives at class
level.

## 8. Refusals (all loud, all named)

| where | when |
|---|---|
| `root()` | explicit slot value on an occupied family; two claimants of the table slot (`weighted` × `tabled`); a repeated instrument slot |
| `enforced` / `capture` | pending suspensions — the owed condition cannot ride the answer |
| `Row.get` / `Row.cell` | a variable the select did not name |
| `Row.get` / `Row.ground` | a cell that is not ground, read into Java: the cell (every such cell, for `ground`) named, the solution rendered whole under its residuals; a selector's map meets it at the cell it reads |
| `Any.get` / `Constrained.get` | a slot name, or a cell under residuals, read as a value off `cell` — refused by its rendering |
| a refusing table | a tabled call under a plain weighted ring |

## 9. Lineage

One arc (September 2026): `Package` → `Knowledge`; the vocabulary move
into `solving`; `Answer<R>` promoted as `Call<R>`'s dual with pldb
re-layered on it; the Query skeleton; an ask stage built, corrected to
enforce (the reify-parity challenge), reshaped twice (`AnswerSet`,
solve-once-ask-many) and torn down for the `select`/`Row` pipeline (`Row`
was `Solution` for a few days in October); the
slot list with the open `slot(Packaged)` door; the weighted slot folding
`Weights`' three hand-built roots; the door cutover (eight `Goal.solve`
faces deleted, ~495 call sites across logic/pldb/library migrated); the
`Question`/`GoalProducer` rewire. Nine hand-assembled roots existed when
the arc opened; zero remain.

October 2026, the mapped reading: the cargo port's row types went
through `Structural` with `Unifiable` members (unsound — the reifier
rebuilds with Anys), then `Term` members with a cast at every use in a
rule body (honest, heavy), before the human named the actual shape: the
engine's inputs and outputs differ in meaning and Java's do not, so the
Java side needs one type that is both, and that type is the variable
read through a `Row`. `Selector` landed in `solving` as part of the
Query API, `Selection` became `Selection<D>`, `solutions()` became
`stream()`, and pldb's `Schema` became a Selector with columns. Then
`Row.get` became the gate itself and `rows()` a terminal beside
`stream()`, after a day in which the map had come off the Selector: the
fault that day was chasing — every reading that was not the type's
needing a door of its own — was the typed stream being the only
terminal, not where the map lived.
