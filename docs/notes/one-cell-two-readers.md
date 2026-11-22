# Conditions, bounded weights and aggregates are one cell with two kinds of reader

- **status**: argued — a design conversation (November 17–22 2026), no
  code
- **evidence held**: derivation, plus a reading of `solving/`
  (`Condition`, `Residues`, `JoinMap`, `Answer`), `tabling/` (`Tabling`,
  `TablingMode`, `Streaming`, `Table`, `TableEntry`), `weight/`
  (`Closed`, `Weights`), `aggregate/Aggregate`, `constraints/Subsolve`,
  and `constraints/store/Theory` on November 21–22 2026. Scratch probes
  of the hand-over were run and not kept (November 22 2026); they are
  described under "The aggregate". The prior art below is recalled from
  memory, not looked up
- **imports**: Call, Answer, Row, Condition, conjunct, Residues, Theory,
  Atom, JoinMap, Table / TableEntry, seal, anonymous master, inside vs
  outside reader, factor(ring, w), WeightedCondition, suspension,
  Subsolve (the closed read), Footprint, Pin. NEW, entries owed if
  adopted, each needing the human's ruling on the name: **piece**,
  **bounded value**, **owed call**, **ripening**, **aggregate** in its
  new sense. They are defined under "The pieces". **complement** (proposed in the glossary, unrated) has no job
  in this design; see "Set aside". No theory is imported; the systems
  under "Prior art" locate the idea and carry no receipt
- **obligations**:
  1. **The carrier, bounded.** `WeightedCondition<W>` as a pure value
     over `Residues`, with `W` a `BoundedSemiring`: pieces, ⊕ with a
     drop rule that compares values as well as regions, ⊗.
     - the "present" instance passes `ConditionLawsTest` unchanged;
     - plain tabling runs on it with the plain suites green, and the
       `plain` flag in `Streaming` and the two `instanceof Condition`
       tests go (`tabling/` is stop-and-ask);
     - a tabling benchmark stays within noise of today.
  2. **Reading a piece.** The rules under "Reading a piece", each with
     a receipt: a conditional row reads as the best of every piece that
     covers the point; a piece whose region holds no point contributes
     nothing when a value is read out; a value is handed over only from
     pieces that owe no witness.
  3. **Owed calls.** The work of `suspensions-cross-as-schemas.md`,
     with the reading this note adds: an owed call is a Call that has
     not been asked (`constraints/` is stop-and-ask).
     - identity is the name and the watched terms; its Residues is the
       knowledge about those terms, cut from the piece's region and
       still narrowing;
     - a call key drops owed calls, an answer keeps them;
     - decide where an owed call is tested: during the search, or
       where the answer is read, as PR #18 settled for verifiers.
  4. **A factor from a term.** `factor(ring, x)` where `x` is a
     variable; the factor is an owed call until `x` is bound, so it
     rests on 3.
     - the receipt is the worked example below, checked against a
       brute-force answer.
  5. **The aggregate as an outside reader.** The settled value of a
     cell handed to a term at the seal; refused for an inside reader.
     - `Aggregate` rebuilt on it, its existing tests green; `sum` and
       `count` stay over ground, distinct solutions and keep refusing a
       conditional answer;
     - settle which comment is stale: `Aggregate` says a fold over a
       tabled recursive goal "would see a partial answer set",
       `Subsolve` says it reads after the seal is certified;
     - find out whether the refusal of a posted rule that reads its
       enclosing rule holds by name or by key (the bill of materials
       needs it by key);
     - pldb's `.aggregating` terminal and its pushdown get their own
       note in pldb.
  6. **Record the gate change.** `weighted-tclp.md` §10 gates its steps
     2–6 on a customer. Building these for the sake of one concept is a
     change of that gate, and that document must say so, along with
     the narrowing to bounded values.
  - Not here, by the human's ruling (November 22 2026): the call graph
    is separate work with its own note, and the closed mode stays as it
    is, refusing a conditional answer.
- **links**: `../design/weighted-tclp.md` (the design this revises: its
  carrier and its drop rule stand; its sealed and closed schedules, its
  enumeration and its level-set delivery are set aside here),
  `suspensions-cross-as-schemas.md`, `../reference/condition.md` §6 (inside and outside readers) and §8.5
  (negation as the ultimate outside reader), `watched-revise.md`,
  `lattice-three-way.md`, `call-dependency-graph.md` (separate work:
  who asked whom, and understanding an answer),
  `../pldb/docs/notes/table-as-the-source.md`

## The claim

An answer is a term with pieces. A piece is a region, the value that
holds on it, and the calls it still owes. A cell folds the pieces that
arrive for a term. A reader is inside the recursion, where it takes
every arrival as it comes, or outside, where it takes the settled cell
at the seal.

Condition, weighted tabling and the aggregate are this one shape:

| Today | In this design |
| --- | --- |
| Condition | a piece whose value is "present" |
| Weighted tabling | a piece whose value is bounded |
| Aggregate | an outside reader that folds the settled answers and hands the result to a term |

Negation over a rule and pldb's posted rule literal are outside readers
already.

## Why now, in the human's words

- "Conceptual clarity and unification" has driven this engine before,
  beside customer triggers.
- Aggregates and weighted inference are the parts whose practical use
  is least understood. Building them into one concept, and teaching it,
  is how the negation rules became clear while Part 4 of the tutorial
  was written.
- It forces the query and answer door to be designed once. That door
  shows up at the table, the Query, the literal, the source and the SQL
  compilers.

## The pieces

| Name | What it is |
| --- | --- |
| Call | a relation, its arguments, and the Residues about those arguments |
| piece | one region of an answer, with its value and its owed calls |
| WeightedCondition | the pieces of one answer term |
| bounded value | a value from a `BoundedSemiring`: a best exists, and going round a loop adds nothing |
| factor | puts a value into a derivation, from a constant or from a term |
| owed call | a suspension in schema form: a name, its watched terms, and the Residues about them, still narrowing |
| ripening | an owed call's Residues narrowing until the relation accepts the pattern |
| outside reader | takes only settled values, at the seal |
| aggregate | an outside reader that folds the settled answers and hands the result to a term |

What an answer carries rides the branch: its region, its value and its
owed calls. They must combine and must cross every door, and they die
with a branch that fails.

## Why bounded values only

The human's reason: the number of derivations of an answer is a
property of how the model is written and not of the domain. Add a
redundant rule and every such number changes while the answers stay.
Where a multiplicity means something, the thing counted can be named,
and a name is part of the answer.

What the narrowing buys:

- Two pieces that overlap need no splitting and no enumeration. A
  piece is not made by cutting a Condition: it is one conjunct, as it
  arrives from one derivation, with its value. A point covered by two
  pieces takes the better value, and since taking the better of a value
  with itself changes nothing, nothing is counted twice. That half is
  idempotence. Boundedness adds that a loop settles.
- Mixing is free: a product of bounded rings is bounded
  (`SemiringStore.boundedProduct`).
- The carrier needs one solving method, the fold while exploring.

The same line shows up three times, and it is one line. A cell and a
table work with contributions that may be seen twice and with sets that
are not yet final: two derivations of one answer, two pieces over one
point, a trip round a loop. A bounded fold tolerates all three. `sum`
and `count` tolerate none, so they run only where each solution is
distinct and the set is sealed. A region the caller narrows later is a
fourth case, and there the two differ less: neither may hand a number
to a term before the kept columns are bound (see "A narrowing that
comes later").

What is in the class: present or absent; cheapest cost without negative
costs; earliest arrival; most likely single path; widest path; the
smallest sets of facts that suffice for an answer. What is out of
the carrier: counting, probability as a sum over proofs, costs that can
be negative, and every fact an answer rests on collected without limit.
The closed mode still serves a ring with a star over ground answers, as
today.

## Reading a piece

- **A conditional row.** Its pieces may overlap. The value at a point
  is the best of every piece that covers it.
- **An empty region.** Propagation is not complete, so a region can
  look inhabited and hold no point. When a value is read out to a term,
  such a piece must contribute nothing. That takes finding one point,
  not listing all of them.
- **A piece that still owes a witness.** A piece may hold a condition
  on a body local that nothing ever decides. For "present" the row
  shows it and that is honest. For a value handed to a term it is not
  enough: "250 if some witness exists, else 300" is not a number. A
  fold is handed over only from pieces whose region is settled once the
  kept columns are bound; otherwise the hand-over stays owed. This rule
  is reasoned and has no receipt; the limit example does not exercise
  it.
- **The drop rule.** `Condition.orConjunct` drops a region inside
  another without looking further. With values, the inner piece drops
  only if its value adds nothing to the outer one. `Streaming.capture`
  names the trap today: "an entailed-but-cheaper answer would silently
  lose its value on the overlap".

## Owed calls

`suspensions-cross-as-schemas.md` gives a crossable suspension the
identity "(name, watched actuals)". That is the shape of a Call. An
owed call is a Call that has not been asked.

- **Its Residues** (the human's reading) is what is allowed to ripen:
  the knowledge about its watched terms. For an asked Call that
  knowledge is frozen at the moment of asking (`Residues.about`). For
  an owed call it is still narrowing, and it is cut from the piece's
  region when needed, not stored a second time.
- **The wake test** is a test on that knowledge: is this a pattern the
  relation accepts. A factor accepts a bound cost. A source may accept
  a bound input argument.
- **Calls drop, answers keep.** `Residues` states the polarity for
  stores: "keys may widen (consumption filters), answers may not drop".
  A key that drops an owed call is wider, which is sound. An answer
  that dropped one would claim more than was derived.
- **Why it is not a store citizen.** An Atom, an owed call and a Call
  all have the shape of a name over terms. An Atom is held by a store
  and may only narrow. An owed call may branch into several answers
  once it is asked, so it stays with the driver.

Two points are open. A suspension today wakes on a binding and cannot
see a narrowed domain; a wake test on Residues can, which reopens the
question PR #18 settled for verifiers. And `Theory.split` puts an atom
in the cut only if every name it watches is given, so "everything the
watched terms touch" has a narrow reading and a wider one that follows
couplings.

## A narrowing that comes later

Knowledge only grows along a branch, so the question is what was
decided on knowledge that has since narrowed.

- **An owed call waiting.** Its Residues is read from the live region,
  never copied, so it cannot go stale. Its wake test must be one that
  stays true once it is true as knowledge grows ("this term is bound").
- **A call already asked.** It was asked under a wider region than the
  caller has now, so its answers are a superset. Each answer meets the
  caller's current knowledge when it is consumed, and the ones that no
  longer fit fail there. Tabling does this for every Call today.
- **A value on a piece.** It stays attached to its region. A later
  narrowing kills the piece or leaves it, value and all.
- **An outside reader that hands on an absence.** "Nothing in this
  region" stays true when the region narrows. If the region was not
  empty the exclusion stays owed, and it is verified again where the
  answer is read, as today.
- **An outside reader that hands on a fold.** A fold handed to a term
  is one number, and a later narrowing can make that number wrong. Take
  the cheapest leg of a cargo up to a limit, asked while the limit is
  only known to lie in 1..5, with leg 0 costing 300 and leg 1 costing
  250. Leg 1 counts only from limit 2 up, so the answer is 300 for
  limit 1 and 250 above it. There is no one number to hand over while
  the limit is open, for `min` as little as for `sum`.
  - So nothing is handed to a term until the kept columns are ground.
    The aggregate waits, as an owed call, or refuses as `Aggregate`
    does today. A body that lists the values of a kept column, as a
    stored literal lists tracking ids, gives one group per value and
    never has to wait.
  - What waits is the settled cell, not a number. Here it holds two
    pieces: `(limit ∈ 1..5 : 300)` and `(limit ∈ 2..5 : 250)`. When the
    limit is bound, the reader takes the pieces that cover it and folds
    them: 300 at limit 1, 250 at limit 3.
  - The difference between the folds is what the cell may forget while
    it waits. A bounded fold drops a piece that another covers with a
    value at least as good. `sum` and `count` must keep every distinct
    solution with its condition, and each solution must be ground apart
    from that condition, or the count needs enumeration.
- **A bounded value that is never handed to a term** rides its pieces
  through every door. A caller that narrows the limit to 1 kills the
  second piece. A caller at limit 3 gets both, as two branches, and the
  next fold takes the better one. That is extra search, not a wrong
  answer.

## A factor from a term, and the worked example

`Weights.factor(ring, weight)` takes a Java value fixed when the goal
is built, and every test in `weight/` passes a constant. A factor over
a variable reads the value from wherever the variable is bound, a
database row included. A body promises no order and a posted lookup
narrows a column long before it binds it, so the factor waits as an
owed call.

The term a factor reads stays an ordinary term, and any rule may read
it: `cost > 100` on a leg's cost is a statement about that leg. What no
rule can read is the running value of a derivation and the cell's fold.
That is what makes dropping a worse piece safe: a rule that filtered on
the running total could have kept the piece that was dropped. A program
that wants a readable total threads it as a term, and then each total
is its own answer and nothing is dropped.

The example the first four obligations must run: legs are rows with a
cost, the network has a cycle, and the question is the cheapest route
between two ports.

- As an aggregate over `route(from, to, cost)`: every distinct cost is
  its own answer, and around the cycle there is no last one.
- As a weight today: the cost cannot be read from the row.
- In this design: `leg(db, from, to, cost)` binds the cost,
  `factor(TROPICAL, cost)` takes it, the cell folds with `min` and
  settles, and an aggregate hands the settled cost back as a number.

A departure window as a finite-domain constraint makes the answer
conditional, which the weighted modes refuse today. None of this has
been run.

## The aggregate

An aggregate is the way a value leaves a cell and becomes a term. It
stands outside, because a value read inside the recursion that
produces it can still move: `cost = 300` is true now and false when a
cheaper route arrives.

What makes an aggregate non-monotone is the hand-over of the value to
a variable, and nothing else in it. The human's proposal (November 22
2026): do the hand-over in an owed call. The aggregate then never
refuses for an open kept column. It is owed, as an exclusion is owed in
Part 4 of the tutorial, and the answer carries it and shows it as a
condition, the way a row shows a witness.

- **A probe of it** (November 22 2026, scratch, not kept): today's
  `Aggregate.min` wrapped in `Projection.project(limit, …)`, so that it
  waits for its limit. With the limit in 1..5 the enforced reading gave
  five ground rows, 300 at limit 1 and 250 above, whichever of the two
  goals was written first; with `best = 300` stated first it gave the
  one row at limit 1. Read raw, or with the limit free, it refused with
  "an answer may not be asked while suspensions pend". So the waiting
  works with what exists, and the refusal marks exactly where owed
  calls must be able to ride an answer.
- **The human's form of it: run the body eagerly, let only the
  hand-over wait.** The body is asked once with the kept column open,
  and its solutions are held with their conditions. The hand-over is
  the only owed part. A second probe (same day, scratch, not kept): the
  body as a pldb rule `legBelow(trackingId, limit, index, cost)`, asked
  once with the limit free, then the hand-over of the first probe.
  With the limit in 1..5 the five rows came out as before and the body
  ran once, where waiting first ran it five times. Each hand-over was
  served from the sealed entry of the open call.
  - The single run was forced with `Aggregate.findall`, for want of a
    plain way to ask a rule and keep nothing. Written after the domain
    was stated, that `findall` refused: "closed sub-solve touches
    pre-existing variables <limit>", although its body never mentions
    the limit. Whether the watermark is right to refuse there is not
    understood.
  - The eager form needs a body that can run with the kept column
    open. `lss(index, limit)` can. A body that itself must wait leaves
    an owed call in its solutions, which is obligation 3 again.
- **What it waits on, in general.** Two things, and never the result.
  - The seal of the body's entry, so the set of solutions is whole.
    That is the wait an outside reader does today.
  - The kept columns that some held solution leaves open, until what
    is known about them decides every solution: in or out. A solution
    with no condition on a kept column needs no waiting, which is the
    group-by case. Binding the columns always decides; a narrowing may
    decide earlier (a limit known to be at least 2 already settles both
    legs), but a suspension today wakes on a binding only.
  A bound result with an open kept column decides nothing by itself.
- **The other direction** comes from the kept column's domain. When it
  is finite, one fold per value gives candidate rows of (kept value,
  result), and those are what pldb's posted lookup holds today: a known
  result then narrows the kept column, and labelling lists the rows.
  This is a hunch; it has not been tried.
- **What still refuses**: an aggregate that reads the recursion it is
  part of, which no waiting can settle; `count` or `sum` over solutions
  that are regions; and a read into Java of a result that is still
  owed, which is the existing rule of `Row.get`.

It is uniform with weighted tabling in mechanism and not in class.
`sum` and `count` are not bounded. They stay legal at a seal, over
ground and distinct solutions, as today.

The human's suspicion, followed one step: `Aggregate` may be removable
whole, with a rule literal doing the work of the sub-solve. A posted
rule literal in pldb is an outside reader already, it starts the rule
on a clean package so only the head's arguments reach the body, and a
posted rule that reads its enclosing rule is refused by name today
(pldb, `PostedRuleInsideABodyTest`, November 17 2026, not committed).
The face proposed for it is a fourth terminal on the literal builder,
`.aggregating(…)`: the head's columns are the kept ones and the result,
a free kept column gives one row per group, and a body of one stored
literal can go to the source as one statement.

## What is already there

Read in the code, November 21–22 2026:

- `Condition` is a bounded semiring value.
- `JoinMap` is one cell for every mode, generic in the ring.
- `Streaming` is one class for plain and bounded-weighted tabling; the
  difference is a `plain` flag, two branches and one refusal.
- A region is a map of Theories whose `meet` and `leq` are plain value
  operations, so the carrier needs no engine contact.
- The outside reader already waits for the seal (`deliverSealed`).
- pldb's posted lookup already carries the Residues about its
  arguments in its call key.

## What it retires

- The `plain` flag, both type tests, the aggregate's private sets and
  the separate reading doors of `Weights`.
- Five refusals of "the owed condition cannot ride the answer", which
  become one rule about owed calls.

## What it leaves alone

The closed mode: `Closed`, `StarSolve`, `DependencyGraph`, `Recurrent`
and `Fragment`. It keeps its cell of "present" values and its refusal
of a conditional answer. The carrier of obligation 1 must leave it
working, and its tests are part of that obligation's check.

## What it does not buy

- Probability as a sum over proofs, and counts of derivations.
- `count` or `sum` over conditional answers.
- Reading a value inside the recursion that produces it.
- An explanation of an answer. That is the call graph's work.

No user asks for the capabilities today. The payoff held now is none;
the payoff promised is coherence, the worked example, and a face for
aggregates that reaches the database.

## Set aside, with what reopens each

- **Enumeration as a fold, and region difference by complement.**
  Reopens when `count` or `sum` over conditional answers is wanted.
- **Values that accumulate, and one exploration valued per ring.**
  Reopens when a program needs a count of derivations or a probability
  that cannot be restated over named solutions.
- **Minimal witnesses as a value.** Bounded, so it fits. Reopens when
  keeping a stored row current on a delete is wanted.

## Prior art

Weighted tabling as an aggregate pushed inside the fixpoint is known in
deductive databases (recalled: Zaniolo on `min` and `max` in recursion;
Ross and Sagiv on monotonic aggregation). Aggregates through
constrained tabling for the lattice family: Arias and Carro, Ciao.
Lattice values with a declared fold: Flix. Calls that wait for a
pattern the relation accepts: modes and delay declarations in Prolog,
access patterns in data integration.

## The cheapest way to kill it

- **The "present" instance is slower or not equal.** Obligation 1's
  checks, before anything rests on it.
- **Pieces pile up on the worked example.** `Residues` keeps body
  locals as witnesses that are "conservatively incomparable across
  answers", so regions carrying them rarely drop. If the cycle does not
  settle under a constraint, the design needs more than a drop rule.
- **A real wake test cannot be rebuilt from a schema.** The kill that
  `suspensions-cross-as-schemas.md` names; it takes the factor from a
  term with it.
- **The settings need code branches.** If the cell ends up with a mode
  switch inside it, the `plain` flag has only moved.
