import SimpleListPage from '../common/SimpleListPage';

export default function PurchaseOrderPage() {
  return (
    <SimpleListPage
      title="Purchase Orders"
      endpoint="/purchase/purchase-order"
      breadcrumbs={[{ label: 'Purchase' }, { label: 'Purchase Order' }]}
      columns={[
        { field: 'poNo', headerName: 'PO No' },
        { field: 'supplier', headerName: 'Supplier' },
        { field: 'status', headerName: 'Status' }
      ]}
    />
  );
}
