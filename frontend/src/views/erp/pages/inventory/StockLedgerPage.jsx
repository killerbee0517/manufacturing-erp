import SimpleListPage from '../common/SimpleListPage';

export default function StockLedgerPage() {
  return (
    <SimpleListPage
      title="Stock Ledger"
      endpoint="/inventory/stock-ledger"
      breadcrumbs={[{ label: 'Inventory' }, { label: 'Stock Ledger' }]}
      columns={[
        { field: 'item', headerName: 'Item' },
        { field: 'inQty', headerName: 'In Qty' },
        { field: 'outQty', headerName: 'Out Qty' }
      ]}
    />
  );
}
