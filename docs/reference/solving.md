# The solving front door — Query, Selection, Row

**Status: AS BUILT** (September 2026, the solving arc). Authoritative for
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
  .select(vars...)        the row pipeline: Selection → rows() → Row
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

`select(vars...)` names the projection and returns a `Selection` with the
reading still open, because different consumers want different answers
from the same question:

- **extraction** — enforced (default) or `raw()` (regions as derived).
- **multiplicity** — distinct (default: alpha-equal images fold in a
  `JoinMap`, conditions join by ⊕, absorption prunes) or `all()` (one row
  per derivation).

The distinct reading needs exhaustion, so its stream computes at the
`rows()` call; `all()` stays lazy. Folding by default loses nothing: the
derivation view survives whole at `run()`.

## 5. Row

One solution's cells, read by the variable itself:

```java
<T> Reified<T> get(Term<T> key)   // the key carries the type
```

The cell comes back in output vocabulary — a decided cell is a value
(`isVal()`/`get()`), a cell the solution left open is its `Any`, so
wideness is a visible fact (couplings between wide cells stay visible)
and is never smoothed into an empty. An unselected key refuses.
`getCondition()` is the knowledge the row holds under; `answer()` is the
whole seam artifact for anything that ships rows onward.

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
it never imports upward) and the faces (`Query`/`Selection`/`Row`, above
everything — they import tabling and weight). The package-granularity
cycle with tabling and weight is accepted; the discipline lives at class
level.

## 8. Refusals (all loud, all named)

| where | when |
|---|---|
| `root()` | explicit slot value on an occupied family; two claimants of the table slot (`weighted` × `tabled`); a repeated instrument slot |
| `enforced` / `capture` | pending suspensions — the owed condition cannot ride the answer |
| `Row.get` | a variable the select did not name |
| a refusing table | a tabled call under a plain weighted ring |

## 9. Lineage

One arc (September 2026): `Package` → `Knowledge`; the vocabulary move
into `solving`; `Answer<R>` promoted as `Call<R>`'s dual with pldb
re-layered on it; the Query skeleton; an ask stage built, corrected to
enforce (the reify-parity challenge), reshaped twice (`AnswerSet`,
solve-once-ask-many) and torn down for the `select`/`Row` pipeline; the
slot list with the open `slot(Packaged)` door; the weighted slot folding
`Weights`' three hand-built roots; the door cutover (eight `Goal.solve`
faces deleted, ~495 call sites across logic/pldb/library migrated); the
`Question`/`GoalProducer` rewire. Nine hand-assembled roots existed when
the arc opened; zero remain.
