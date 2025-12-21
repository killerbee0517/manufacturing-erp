import SimpleListPage from '../common/SimpleListPage';

export default function StockTransferPage() {
  return (
    <SimpleListPage
      title="Stock Transfer"
      endpoint="/inventory/stock-transfer"
      breadcrumbs={[{ label: 'Inventory' }, { label: 'Stock Transfer' }]}
      columns={[
        { field: 'transferNo', headerName: 'Transfer No' },
        { field: 'from', headerName: 'From' },
        { field: 'to', headerName: 'To' }
      ]}
    />
  );
}
