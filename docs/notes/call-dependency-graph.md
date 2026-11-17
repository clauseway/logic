# The table can record which call read which — one record, kept at the tabled call

- **status**: argued — a design conversation (November 15–17 2026), no
  code
- **evidence held**: derivation, plus a reading of `Tabling`, `Table`,
  `TableEntry`, `InBody`, `TablingMode` and the weight package's
  `Closed` on November 17 2026. Nothing was run. The prior art below is
  recalled from memory, not looked up
- **imports**: Table / TableEntry, Call, anonymous master, reader /
  consumer, inside vs outside reader, InBody, ambient scope, subsumptive
  reuse, JoinMap, seal, group seal, Footprint, Pin, GoalProducer,
  AnswerSource, residence. NEW, entry owed if adopted: **call dependency
  graph** — the human's conversational name is "dependency graph", which
  collides with the weight package's `DependencyGraph` class (the star
  equation graph, a different object at a different grain); the name
  needs the human's ruling. **incremental tabling** ⋯import — no
  receipt yet
- **obligations**:
  1. **Build the record.** In `tabling/` (stop-and-ask): `InBody`
     carries the call of the entry whose body it stamps, the tabled
     call notes (consumer's call → consumed entry's call) on the
     `Table`, and a test prints the graph for a recursive `path` and
     for one mutual-recursion ring.
  2. **The negative witness.** A test where a body branch reads entry
     B and then fails: the edge is still there, where the closed
     mode's per-derivation record has none.
  3. **pldb's half.**
     - carry the consumer's identity across `GoalProducer` (it runs
       the rule on a clean package today, so the edge would land on
       the root);
     - file each source read under the consuming call;
     - state the law as a test: the graph's leaves equal the
       transaction's Footprint regions.
  4. **Two measurements, before anything is built on it.**
     - the cost of the record on a tabling benchmark;
     - the grain on a real solve: print the graph of one cargo
       `Delivery` derivation and see whether every entry reaches
       every leaf.
  5. **Reuse across solves.** TRIGGER-GATED by the standing rulings in
     pldb's `table-as-the-source.md` and `domain-layer.md` (a measured
     rerun cost that hurts); it gets its own note when it reopens.
  6. **The import receipt.** A theorem-import receipt for incremental
     tabling before any doc leans on it.
- **links**: table-completion.md (the ambient scope and the sleeper
  edge), star-tabling.md (the existing per-derivation record),
  semiring-inference.md §7b (a completed entry with zero answers is a
  cached "no"), condition.md §8.6 (the annotation seam — the finer
  grain this note does not reach), `../pldb/docs/notes/`
  table-as-the-source.md, pins-stay-off-engine.md, transaction.md,
  `../pldb/docs/design/domain-layer.md` §5.3

## The claim

A solve's Table already holds every tabled call as an entry named by
its Call. What it does not hold is who read whom. If the tabled call
notes one pair — the call whose body is running, and the entry it is
about to read — the Table becomes a graph over calls that can be
walked after the solve. That one record answers three questions the
engine cannot answer today: what did this call depend on, why is this
entry empty, and which entries a change to the data could have
touched.

The grain is the call. It is not the answer and not the derivation.

## What is already there

Read in the code, November 17 2026:

- **Nodes.** `Table.entries` maps each `Call` to its `TableEntry`; the
  entry's channel is named by the call.
- **One door for every read.** Every caller, the first included, goes
  through `Tabling.tabled` and reads as a consumer. The consumed entry
  is in hand there, either the caller's own or the wider one that
  subsumptive reuse picked.
- **The body knows it is a body.** `Table.bodyState` stamps `InBody` on
  the master's starting package, once per master, and every package
  derived inside the body carries it.
- **A finer record exists for one mode.** The closed mode threads
  `Recurrent` through the package: `absorb` adds the consumed
  (entry, answer), `capture` reads it when an answer is cached and
  writes a base or an edge into the weight package's
  `DependencyGraph`.
- **A cached "no".** A sealed entry with no answers already serves
  every narrower call.

## What is missing

- **Which body.** `InBody` is one shared marker. It says "inside some
  tabled body", not whose. The tabled call can see that its caller is
  inside a body and cannot name the entry.
- **The record.** Nothing keeps the pair after the read.
- **Leaves.** Logic has no base data. A read of a source happens in
  pldb and is not a table entry.
- **Life past the solve.** The Table is seeded per solve at the root
  and dropped with it.

## The record

`InBody` carries the call of the entry it stamps. At the tabled call
the reader's package gives the consumer (its `InBody`, or the root
when there is none) and the lookup gives the consumed entry. The
Table keeps the pair in a concurrent map from call to the set of
calls it read. The cost is one map write per tabled call, not per
answer.

Two rulings the grain forces:

- **Record at the call, not at capture.** The closed mode's record is
  written when a derivation succeeds. A branch that read an entry and
  then failed leaves nothing there, and that read is exactly the one a
  later change can turn into an answer. `pins-stay-off-engine.md` makes
  the same point about absence reads in dead branches. So the closed
  mode's record cannot be reused for this, and this record cannot
  replace it: star solving needs answers and coefficients, this needs
  neither.
- **Thread the identity through the package, not the frame.** The
  glossary rules a package-threaded LEDGER out, because counters fork
  with the world. An identity set once at body entry and never changed
  is not a ledger; every branch of the body agrees on it. `Recurrent`
  already rides the package the same way.

## Leaves, and the Footprint

A leaf is a read whose answers come from outside the solve. Logic can
name the consumer; only pldb sees the read. Two gaps sit there
(obligation 3): `GoalProducer` starts the rule on a clean package, so
the consumer's identity stops at that boundary, and a sync source read
is not an entry at all.

The Footprint keeps its job. It is the commit's proof: the Pins are
minted at the source and the map is one per transaction. The graph is
a second view of the same reads, filed by call. Flattening the graph
must give the Footprint's regions; that is the law test, and a
difference in either direction is a bug in one of the two records.

## What it serves, in the order it could be built

1. **Seeing a solve.** Print the graph: which calls ran, which were
   served by a wider entry, which sealed empty.
2. **Why, and why not, at call grain.** Why: walk down from a call.
   Why not: walk down to the empty entries and the leaves that
   returned nothing.
3. **Reuse across solves.** Keep sealed entries; after a change, drop
   every entry that reaches a changed leaf and keep the rest. An entry
   is kept whole or dropped whole. A ring sealed as a group is kept or
   dropped together. If a recomputed entry seals with an equal
   JoinMap, the calls above it need not rerun. A kept entry is true
   only in the world it was computed in, so it must carry that world's
   Pins.

Step 3 is gated. pldb's `table-as-the-source.md` rules cross-solve
reuse of a rule's extension as "none engine-side" and names the
reopening trigger; `domain-layer.md` rules truth maintenance out until
a workload shows full recomputation is unacceptable. This note argues
that the record makes step 3 possible. It does not argue that the
trigger has fired, and it has not: there is no measured rerun cost.

## What it does not buy

- **Why this answer.** That is answer grain: the provenance semiring,
  which waits on weighted TCLP.
- **Why this branch failed inside a body.** The graph sees calls, not
  bindings or narrowings. That is the annotation seam, which stays
  parked.
- **Clause learning.** A sealed empty entry is a learned "no" for a
  call pattern and nothing finer.
- **When to re-run a nogood's trial.** A trial reads variables and
  domains, a different grain. The one shared corner is a nogood whose
  literal is a tabled call: its trial reads an entry, and that read
  could be noted here.
- **A dataflow library for code outside logic.** Keeping the record at
  `Channel.suspend` in functional would give every channel user the
  edges. This note assumes tabling only: the consumer there is a Call,
  which is what the three uses above need, and no code outside logic
  asks for it.

## Prior art

XSB and SWI-Prolog ship incremental tabling: a graph over tabled calls
and the dynamic facts they read, entries marked invalid on assert or
retract, recomputed on the next call, and a stop when the recomputed
answers are unchanged. Steps 1–3 above are that design. It differs
here in three places: one database changed in place where this engine
has pinned worlds, nothing kept across a restart, and (a hunch) no
coverage of entries that hold constrained answers.

## The cheapest way to kill it

- **The grain is too coarse to be worth keeping.** Print the graph of
  one real derivation. If every entry reaches every leaf, a change to
  anything drops everything, step 3 saves nothing, and "why" answers
  with the whole program.
- **The record costs more than the reruns it would save.** Both
  numbers are unmeasured.
- **The identity does not survive the body.** If some path runs body
  work on a package that lost its `InBody` — `GoalProducer` is one
  known case — edges land on the root and the graph is wrong without
  saying so. Obligation 2's test and the law test are what would show
  it.
