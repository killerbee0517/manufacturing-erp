# Production Module (Process Templates & Batches)

## Concepts
- **Process Template**: Defines the flow for producing an item. Includes an optional code, output item/UOM, default input materials (can be multiple) and ordered steps.
- **Batch Run**: An execution of a template. Batches start in `DRAFT`, can be `RUNNING`, and are `COMPLETED` after outputs are posted.
- **WIP vs FG**:
  - **WIP** outputs stay in the production workspace and can be selected as inputs for another batch.
  - **FG** outputs move into a godown (stock) when produced.
- **Inventory Movements**: Every issue/output records into `inventory_movements` (and stock ledger for FG) so balances can be computed.

## Flow
1. Create a **Process Template** with steps and default inputs.
2. Create a **Batch** from a template (optionally set planned output qty/UOM).
3. **Start** the batch to generate runtime steps.
4. **Issue materials**: multiple inputs from Godown or WIP; updates movements and WIP consumption.
5. **Process steps**: mark steps done as work progresses.
6. **Produce outputs**: record WIP or FG, optionally choose destination godown for FG.
7. **Complete batch**: only after some output exists; status moves to `COMPLETED`.

## Outputs as Inputs
- WIP outputs keep a balance (produced minus consumed).
- When issuing materials to another batch, pick `WIP` as source and select an available WIP record; the system reduces the available balance.
