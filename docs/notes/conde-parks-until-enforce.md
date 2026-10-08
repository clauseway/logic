# Parking every Conde until enforce removes conjunct order from the untabled fragment

- **status**: argued (October 2026, the human's "fork the conjuncts and
  merge afterwards" probe, redirected in conversation) — not built; a
  slot-first spike is specified below and priced against the existing
  instrument. NOT AN ORIGINAL IDEA: the rule is the Basic Andorra Model
  (1987–91), the exit loop is Oz's stable-then-distribute, the suspension
  form is Prolog II's freeze — § Lineage maps every piece to its source.
  What is engine-specific is the seat (enforce), the determinacy test
  (Doomed, at run time, no analysis), and the tabled-call ruling. Within
  the repo it resurrects the staged fork of the fork-timing arc, which
  won the August campaign and died unrecorded with the disjunctive-store
  branch.
- **evidence held**: derivation; measurement — the three receipts of
  § Receipts (two step-count races and a two-plane profile, run on
  master and re-run on branch infinity-sorts-last, identical to the
  step); plus the measured record it leans on.
  The deletion commit (de1d1d1f) states the three-lane verdict: "fair
  conde wins the static shops 2.2–3x, staged projection wins variable
  spaces 4x, and the bare race converges to a flat 5x loss" for
  residence; constraint.md §3 keeps the one sentence — "the staged fork
  beat every resident lane because it deferred exactly as long as
  deliberation could still progress." The staged projection lane was
  never committed; only those two records survive. The profiler
  decomposition (090d0bd7) itemizes what residence paid (the drain at
  4.3× conde's, 176k vs 41k; imposed trials 70k) and what it won
  (labelling, 13k) — this note keeps the win and never pays the rent.
- **imports**: glossary — enforce (the FINITE exit), labelling, Doomed,
  Barrier / keyed widening, unit propagation (ratified ⋯import), stores
  are branches as data, suspension (for what this is NOT). ⋯import,
  receipts owed on ratification (§ Lineage has the references): the
  Andorra principle / Basic Andorra Model (determinate goals first, fork
  only when none remains); Oz's computation-space loop (propagate to
  STABILITY, then DISTRIBUTE) as the exit loop; coroutining by delay
  declarations (freeze / wait / when / block) as the suspension form the
  staged projection used; constructive disjunction as what this
  deliberately does NOT do; independent and-parallelism and its strict
  independence condition as the probe this note redirected. First-fail
  travels with labelling-order-blind.md's entry.
- **obligations**: (1) the spike, as a Query slot so no existing test
  moves: a `Deferred` store beside `Suspensions` (a persistent list of
  parked goals); `Conde.apply` appends itself and succeeds when the
  store is present, forks as today when absent (one map lookup — zero
  cost absent); the expansion loop owned by Propagation at the four
  seams that today refuse under pending suspensions (reify, capture,
  the tabled master entry, the tabled answer leaving); the barrier
  sites expand before running — `Barrier`, `Condu`, `Conda`, negation,
  closed aggregates, AND EVERY TABLED CALL (§ what it does not buy).
  (2) one first-fail sort at enforce over FD domains and parked condes
  together — the piece that reproduces the 4× (the space domain of size
  two labels before the three-way fork, which then collapses); this is
  labelling-order-blind.md tier 1 plus the merge. (3) the doom sweep:
  before any fork, drop every alternative whose leading postings are
  Doomed, impose a lone survivor as a unit, propagate, repeat to
  quiescence; only then fork the smallest live disjunction — judging
  at the exit, never per wake (one-speculative-judge.md's cost
  discipline), the lawful replacement for the deliberation prototype's
  store-name string check. (4) the instrument: a third lane in
  SchedulingBenchmarkTest (the conde spelling under the slot) and the
  variable-spaces workload restored; pins to hold within the exit-cycle
  cost (~10%): race at five 36.9–37.2k, branch-basis race 32.9–33.5k,
  genesis depth-first 1.6–2.0k; the win to approach: 4× over conde on
  variable spaces. (5) decisions owed the human before a line: slot
  then default, or default; whether tracing keeps eager forks (as it
  keeps depth-first); the kill criterion (the pins above; the ambient
  optimizer's +37% unfold tax is the precedent for an always-on
  mechanism charging more than it earns). (6) weighted inference: ⊗
  must commute under this rule, since fork order stops being textual —
  check the shipped rings; a non-commutative provenance ring is
  excluded. (7) if the pins hold: OrderingOptimizer, DoomPruner,
  CascadingOptimizer and the ambient delivery are deletable as a
  separate change; `Barrier` survives as the one marker meaning "this
  conjunct is control."
- **links**: constraint.md §3 and §6 (the staged fork's sentence; the
  deliberation prototype), disjunction-store-pays-in-products.md (the
  economics, the verdict, the concession this note is the third corner
  of), labelling-order-blind.md (first-fail — this note is where it
  lands), one-speculative-judge.md (judge at commit points, never per
  wake), finite-goal-tier.md (`any` as fork vs posting — this is `any`
  as fork, deferred), optimizer.md §2 (the barrier contract, preserved
  verbatim), ambient-optimizer.md (the unfold tax; the dynamic-ordering
  vs deferred-lookups XOR this note does NOT settle), the suspension
  guard (commit 4f7d1225; Tabling.java's two refusals), tabled-
  constraints.md (Mod-TCLP — the only crossing shape tabling accepts,
  which is why a closure cannot cross).

## The claim

One rule: **never fork while determinate work remains.** A `Conde`
reached inside a conjunction does not branch at its textual position.
It parks itself as data in the Knowledge and succeeds. Postings resolve,
propagators cascade, suspensions ripen. When the branch reaches enforce
with parked disjunctions, it does not emit: it sweeps (doom, units,
propagate, repeat), then forks the smallest live disjunction, and each
child does the same. Conjunction stays a flatMap; Knowledge stays one
threaded value; nothing is ever merged.

Why this removes control: conjunct order can do two things today —
decide which determinate contributions reach the store first (irrelevant
by confluence; the determinate fragment is chaotic iteration over
monotone operators), and decide what knowledge a fork sees (ALL the
control). The rule removes the second by construction: every fork
happens at the fixpoint of every determinate conjunct in the branch,
wherever written. Relational Peano `plus(X,Y,Z) ∧ X≡1 ∧ Y≡2` in any of
its six orders unfolds `plus` with both arguments ground.

Across `defer`: an unfolding is forced by whoever applies the call; its
body's `Conde` parks like any other. A ground-first-argument `appendo`
costs one unit imposition per layer and no branches — each layer's dead
clause is Doomed at its head unification, the survivor runs, its
recursive `defer` forces the next layer, all inside one exit cycle,
before anything forks. Unfolding stays bounded (the recursive call sits
inside an alternative, chosen only by sweep or fork — today's
termination structure), and knowledge made three layers deep is in the
same Knowledge every parked goal is examined against, with no rewrite
hook.

## What it buys, and the honest comparison with the optimizer

On the fragment OrderingOptimizer can see, this IS the ordering pass run
by the engine: postings first, disjunctions last, every fork sees the
segment's knowledge. The benchmark's conde lane is already in that order
by hand (strips post first, pair disjunctions last; the optimizer slot
is off because nothing is left to sort) — and the staged projection
still won 4× on variable spaces against it. The difference is exactly
where the static pass is blind:

- **the end of the segment is not the end of the branch** — the
  optimizer's "end" precedes labelling and every unfolding; the space
  literal is decided when the space domain is LABELLED, and no conjunct
  order puts a disjunction after labelling. Enforce does.
- **determinacy is declared there, discovered here** — `Bounded` is a
  trust surface on leaves; a call determinate after one unfolding, or
  determinate because of bindings made elsewhere, is invisible to the
  sort (its javadoc calls the pass half-blind). The sweep never asks.
  Receipt R2 isolates this: the information is not in the tree.
  (That an opaque relation call is held as a BARRIER is the
  implementation, not the principle — receipt R1 and § The defer leaf.)
- **no rewrite, no unfold tax** — a park is a list append.
- **forks shrink, not just move** — a three-way disjunction with two
  Doomed alternatives is a unit after the sweep; DoomPruner can only
  kill with rewrite-time knowledge, which at the root is nothing.
- **determinate work after a fork runs once** — threaded, every
  alternative re-runs the determinate conjuncts that follow it. Scope
  of the saving, honestly: the posting fragment (posted once, then
  propagating incrementally per child) and whatever does not wait on
  the forked variables; a `project` on the forked variable still runs
  per child under either scheme.

What the optimizer does that this does not: order determinate conjuncts
among themselves for fail-fast — one pass of wasted work at most, never
a multiplied one.

## What it does NOT buy

- **Tabled calls keep their textual control.** A parked disjunction is a
  closure over live names — not a Theory: not renameable, not
  comparable, not carriable in an answer — so it cannot cross the
  tabling barrier, exactly the suspension guard's reason. Calling
  general under parked disjunctions (`member(X,[1,2,3]) ∧ path(X,Y)`
  keyed as `path(_,_)`) is sound (callers filter at consumption) but
  not free: it generalizes every tabled key to the un-forked pattern and
  can turn a finite call infinite. Ruling: a tabled call is a barrier
  site — everything parked in the caller expands before the call, which
  reproduces today's call patterns verbatim and leaves both guards
  untouched (the list is empty when they look); a body's own parked
  disjunctions expand at its answer seam before capture. Consequence
  owned: control-free ∧ stops at tabled calls; between them and in
  untabled code order is gone; across one, textual position still
  decides what the call sees — optimizer.md §2's contract, kept. The
  ambient-optimizer XOR (dynamic ordering vs deferred lookups) is NOT
  settled here: specific keys vs one general entry is a per-call
  policy, and this note takes today's answer.
- **It is not the disjunction store.** No residence, no unit propagation
  per binding, no trial per wake, no Theory citizenship — the store's
  rent (the drain at 4.3×) is never paid; the store's labelling win is
  kept because forks happen after labelling has had its say.
- **It is not a suspension.** A parked disjunction gets its own store so
  the two guards keep their meanings: a parked suspension is an owed
  CONDITION, a parked disjunction is pending SEARCH. (An earlier framing
  as "a suspension whose ripeness is at-exit" is withdrawn.)
- **Not confluent goals keep `Barrier`** — committed choice, negation,
  impure projection bodies, closed aggregates: each a mini-exit.
- **No dominance over the hand order or the optimizer.** What IS
  guaranteed, on the pure fragment: the same answer set (confluence),
  and at any given fork a SUPERSET of the knowledge that fork would see
  under any textual order, hence a subset of surviving alternatives
  there. Not guaranteed: tree size, because forks happen in a different
  order. Three exposures, named: (i) fork order — first-fail on live
  `Bounded` is a heuristic with a good average and no dominance result,
  and a hand-chosen pivot can beat it; (ii) wide propagation — every
  posting enters the widest state the branch will have, so a heavy
  propagator (GAC over large domains, a long bounds chain) may cost
  more once wide than twice ground, labelling-order-blind.md's
  ground-two-compute-one seen from the other side; (iii) termination —
  a determinate conjunct unbounded without the fork's binding
  (`(X≡a ∨ X≡b) ∧ loop(X)`, `loop(X) :- X≡f(Y) ∧ loop(Y)`) terminates
  today and diverges parked; rare (a conde-free recursive relation) but
  real, and the Andorra record names it — Andorra-I's answer was
  sequential-conjunction annotations, which here is `Barrier`. Plus
  latency: time to first answer can rise while total work falls (the
  genesis pin is where it shows). Against the optimizer specifically:
  the pass moves only what it can price and leaves opaque calls in
  place, so (iii) cannot happen under it; on (ii) both are equally
  exposed; on fail-fast among determinate conjuncts the pass is ahead
  by at most one wasted pass.
- **A determinate infinite loop still loops.** Same as today.
- **Traces stop reading in Prolog order**; sibling `Call` ports firing
  up front was the first symptom of this already.

## Receipts (October 2026, fair driver, step counts; scratch tests, not kept)

Three measurements, run on master and re-run on branch
infinity-sorts-last (∞ sorts last, only barriers partition) — identical
to the step on both. The instrument is the engine's own step listener
and the two-plane ScopeProfiler, the same ones the scheduling benchmark
uses.

**R1 — implementation, not principle.** `appendo(X, Y, [1,2,3]) ∧
X ≡ [A]`, one answer.

| spelling | steps |
|---|---|
| as written, no optimizer | 179 |
| as written, OrderingOptimizer (Cascading ∘ Ordering) | 591 |
| hand-swapped, posting first | 93 |

The rewritten tree is `(appendo(...) && X ≡ [A])` on both branches: the
call did not move. Cause: `appendo`'s second clause ends in `defer(...)`,
which is `goal(lambda).named("recursive call")` — an unrecognised leaf,
priced as a barrier; barrier-ness propagates up through the conjunction,
the `Conde` and the name (`anyBarrier`), so the whole relation
partitions its segment and the unification cannot cross it. On master ∞
and barrier were one thing; infinity-sorts-last separates them but a
composite holding a barrier still partitions, and the `defer` leaf is
not ∞, it is UNKNOWN — the fix does not reach this case. With the leaf
recognised (§ The defer leaf) the unification sorts first, the hook then
forces the body with `X` bound, and DoomPruner kills the dead clause:
the optimizer's PRINCIPLE reproduces parking on R1 completely. R1 is
therefore struck as evidence for this note; it stands as a finding
against the implementation.

**R2 — principle.** `p(X) ∧ q(X)`: `p` an opaque call whose body is an
eight-way `Conde`, each alternative binding `X` and running a fixed
`appendo`; `q` an opaque call determinate after TWO unfoldings
(`defer(defer(X ≡ 1))`). Neither declares `Bounded`. One answer.

| spelling | steps |
|---|---|
| `p ∧ q` as written, no optimizer | 1021 |
| `p ∧ q`, OrderingOptimizer | 6903 |
| `q ∧ p`, hand-swapped | 167 |

The tree gives a rewriter nothing to sort on: both conjuncts are
opaque, both price ∞, and with ∞ movable they tie and the stable sort
keeps textual order. That `q` is determinate exists only in the run,
two layers deep. Parking reaches the 167 shape without the swap: `q`'s
unfoldings are determinate and run before the parked `Conde`, and the
sweep then dooms seven of eight heads. The optimizer reaches it only by
a declaration (`Bounded(1)` on `q` — a mode annotation) or by FORCING
(§ The defer leaf, depth two). This is Andorra's claim against
reordering in one measurement: a rewriter orders by what the tree says,
determinate-first orders by what running says.

**R3 — the unfold tax, profiled.** R2's workload under the ScopeProfiler.

| leaf workforce | optimizer off | optimizer on |
|---|---|---|
| recursive call (the `defer` hook) | 140 | 6078 |
| unification | 724 | 628 |
| `appendo` body | 560 | 600 |
| total | 1433 | 7315 |

Every added step lands under the recursive-call leaf and is minted at
`OrderingOptimizer.price` or `Optimizer.visit`: the hook splices
`store.rewrite(body, s)` into the goal's continuation, so the visitor's
FIBER is stepped by the scheduler like search work. Per node, per pass:
a `Fiber.defer` per child in `visitAll`, a `Fiber.zip` + `map` per
child to collect, a `map` to rebuild, a `map` to re-mint the
`NamedGoal`, and in the ordering pass another `Fiber.defer` per named
node — then the pipeline walks the whole tree once per pass. ≈ 20 steps
per node; ≈ 140 per rewrite of `appendo`'s seven-node body, ≈ 1460 for
`p`'s. The rewrite reordered nothing here. This is the +37% of the
August record measured at 5× because the real work per unfolding is
tiny; it is paid whether or not anything moves, and it is NOT tagging —
an eager direct-recursive walk removes it from the step count. A parked
`Conde` is a list append and pays none of it: the one claim in this
note R3 strengthens.

## The defer leaf — the one declaration that is a string

The class of trouble the receipts expose is not tags as such. A `Goal`
is an opaque function, so every property a consumer needs about it is
DECLARED from outside — optional (a lambda declares nothing),
conservative when absent (barrier), contagious upward (one unrecognised
leaf partitions its whole composite), and estimated where present
(`Bounded` is a trust surface). The engine already found the
non-brittle shape once: a `Posting` is determinate because of what it
IS, `Doomed` is read off the value — properties by construction, not by
label (finite-goal-tier.md is the same move for the forking fragment;
goals-as-data.md the full cure). The optimizer is the consumer most
exposed because it needs the most properties; `defer` is the leaf it
most needs and the one that is literally a string.

Give `Goal.defer` its own goal class carrying the supplier. What the
optimizer can then do, in two steps:

- **By type alone — one bit.** "This is a recursive unfolding made
  through the library's door, not an arbitrary lambda" licenses a
  POLICY: transparent widening, unknown order, movable, sorts last,
  never a barrier. A contract ("what you defer is a pure relation
  body"), not knowledge. Safe against the impure citizens the engine
  already names (tabled calls are `Barrier` instances; committed choice
  its own classes); exposes only side-effecting user lambdas hidden in
  a relation body. Enough for R1. Nothing for R2 — `p` and `q` both
  read "deferred, unknown" and tie.
- **By forcing — the door.** The type carries the supplier, so the
  optimizer can force one layer at plan time: allocation, not search —
  the body's tree is built, nothing runs, nothing binds. Then
  doom-sweep the clause heads against the live knowledge (what
  DoomPruner does at the hook already) and classify the call: no live
  clause ⇒ the conjunction is refuted before anything runs; one ⇒
  determinate, price 1, sort first like a posting; several ⇒ price the
  sum, sort last. That is this note's sweep performed speculatively at
  the layer boundary. R2 needs forcing depth two to see `q`'s
  unification; depth one sees another deferred leaf and learns nothing.
  Costs: bodies of conjuncts that an earlier failing sibling would have
  spared get allocated; the forced body must be the one that runs, so
  the rewrite splices it in (as the hook does for the unfolding goal
  today, now for its siblings too); a depth knob appears, and every
  program has a depth at which it is wrong; the pass stays half-blind
  within a layer; and the walk tax (R3) grows with every forced body
  until the walk stops being a fiber.

The comparison in one sentence: with its own type the optimizer can
OBSERVE determinacy by forcing to depth d at plan time, paying
allocation and a parameter; parking observes it by running to
quiescence at enforce, paying nothing up front and needing no
parameter, because the run IS the forcing. Same observation, two
clocks. The type is what lets a planner make the observation at all —
without it a deferred body is a lambda, and the only thing a planner
can do with a lambda is step around it.

## Lineage — prior work, piece by piece

Nothing in the rule is new. The contribution, if any, is the seat and
the test; the rest is recovered, and should be named as such on
ratification.

- **The rule itself — the Basic Andorra Model.** D.H.D. Warren, "The
  Andorra Principle" (Gigalips workshop, 1987, unpublished); Haridi &
  Brand, "Andorra Prolog: An Integration of Prolog and Committed Choice
  Languages" (FGCS 1988); Santos Costa, Warren & Yang, "Andorra-I: A
  Parallel Prolog System that Transparently Exploits both And- and
  Or-Parallelism" (PPoPP 1991). Determinate goals (at most one candidate
  clause) reduce first, in any order and in parallel; a nondeterminate
  goal forks only when no determinate goal remains. "Never fork while
  determinate work remains" is that sentence. Andorra-I's determinacy
  test was COMPILED — Santos Costa, Warren & Yang, "The Andorra-I
  Preprocessor: Supporting Full Prolog on the Basic Andorra Model"
  (ICLP 1991) — which is where its overhead lived; the doom sweep here
  is the same test read off `Doomed` at the exit, no analysis, no
  modes, and it pays nothing per binding. The extension that promotes
  nondeterminate goals when nothing LOCAL remains and lifts what all
  branches agree on is the Extended Andorra Model (Warren, "The Extended
  Andorra Model with Implicit Control", ICLP'90 workshop) and AKL
  (Janson & Haridi, "Programming Paradigms of the Andorra Kernel
  Language", ILPS 1991; the BEAM — Lopes, Santos Costa & Silva, 2003);
  the glossary's "agreement move" is AKL's, and this note stays on the
  Basic side of that line deliberately — the agreement move is the
  machinery the disjunctive store was shelved waiting for.
- **The exit loop — stable, then distribute.** Smolka, "The Oz
  Programming Model" (1995); Schulte, "Programming Constraint Services"
  (LNAI 2302, 2002). A computation space runs its propagators to
  STABILITY (no propagator can run — our agenda quiescence plus the doom
  sweep), and only a stable space is handed to a DISTRIBUTOR, which
  creates the alternatives (our fork at enforce). Oz's disjunction
  combinators (`or`/`dis`) that fire a clause when its rivals are
  disentailed are unit propagation by guards — the part the disjunctive
  store built and this note does NOT rebuild. The whole CP solve loop —
  propagate to fixpoint, then branch — is this shape (Van Hentenryck,
  "Constraint Satisfaction in Logic Programming", 1989); the engine's
  FD labelling has always been the loop's branching half, and this note
  only widens what the branching half may hold from domains to every
  `Conde`.
- **The suspension form — coroutining by delay.** Prolog II's
  `geler`/freeze (Colmerauer, 1982); MU-Prolog's wait declarations
  (Naish, "Automating Control for Logic Programs", JLP 1985; "Negation
  and Control in Prolog", LNCS 238, 1986); NU-Prolog `when`, SICStus
  `block`. A call delays until its arguments are instantiated enough —
  exactly the staged projection lane (`project` on the spaces around the
  non-overlap disjunction) that won 4× in August, and exactly
  `Projection.project` today. The difference from this note: a delay
  declaration names WHICH variables to wait for; enforce waits for
  everything determinate, which is why no declaration is needed and why
  a tabled call must be a barrier (nothing names when it is "ready").
- **The constraint-store reading — ask/tell.** Saraswat, "Concurrent
  Constraint Programming" (MIT Press, 1993): agents on one shared store,
  tell = our Posting through the chokepoint, ask = our ripeness and
  Doomed. Andorra is cc with angelic nondeterminism; the engine's
  determinate fragment IS a cc program already, which is why conjunct
  order was never semantic there.
- **The branching heuristic — first-fail.** Haralick & Elliott,
  "Increasing Tree Search Efficiency for Constraint Satisfaction
  Problems" (AI 14, 1980). Receipt already owed by
  labelling-order-blind.md; this note is where it lands, widened from
  domains to parked disjunctions sorted together.
- **What this note deliberately does NOT import — constructive
  disjunction.** Van Hentenryck, Saraswat & Deville, "Design,
  Implementation, and Evaluation of the Constraint Language cc(FD)" (JLP
  1998): propagate FROM an undecided disjunction what all its
  alternatives agree on. That is the agreement move, the disjunctive
  store's unbuilt future, and the literature's own record of it is
  "mixed, marginal" (disjunction-store-pays-in-products' accounting) —
  the August benchmark reproduced that record. Parking does nothing with
  an undecided disjunction until the exit, by design.
- **The probe this redirected — independent and-parallelism.**
  Hermenegildo's &-Prolog (PhD 1986); Hermenegildo & Rossi, "Strict and
  Non-Strict Independent And-Parallelism in Logic Programs" (JLP 1995);
  Gupta, Pontelli, Ali, Carlsson & Hermenegildo, "Parallel Execution of
  Prolog Programs: A Survey" (TOPLAS 2001). "Fork the conjuncts and
  merge afterwards" is IAP, and the literature's condition is the one
  the conversation re-derived: forking pays only under strict
  independence (no shared unbound variables); with sharing you need
  bindings published mid-flight (dependent AP), and publishing a binding
  made under a sibling's choice point is unsound — only the agreement is
  publishable. Andorra is the resolution the field reached: share the
  store, fork late.
- **The engine's own ancestors.** cKanren's `enforce-constraints` at
  reification (Alvis, Willcock, Carter, Byrd & Friedman, "cKanren:
  miniKanren with Constraints", Scheme Workshop 2011) is the enforce
  seat this note widens. Within the repo: the staged fork of the
  fork-timing arc (constraint.md §3, de1d1d1f's message — the only
  records), labelling-order-blind.md (first-fail), finite-goal-tier.md
  ("`any` as fork" vs "as posting"), the suspension guard (4f7d1225).
  Disambiguation: the repo's "staged fork" names fork TIMING; it is
  unrelated to multi-stage ("staged") miniKanren (Amin, Byrd & Rompf,
  "Staged Relational Interpreters", POPL 2024).

## Cheapest kill

The race at five regressing beyond the exit-cycle cost against the pins
above — the exact instrument that killed residence, with residence's
numbers as the precedent for what a loss looks like — or the restored
variable-spaces lane failing to approach the recorded 4×. Either closes
the note as refuted by measurement.

One sentence: the optimizer's reorder is a plan-time guess at "fork
after the knowledge"; parking every `Conde` until enforce makes the
guess exact and dynamic for everything but tabled calls, which keep
their barrier — the Basic Andorra Model with Oz's exit loop, seated at
enforce with Doomed as the determinacy test; the only lane that beat
residence in August was this rule, applied once by hand.
