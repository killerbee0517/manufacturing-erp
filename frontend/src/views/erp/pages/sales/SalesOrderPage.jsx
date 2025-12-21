import SimpleListPage from '../common/SimpleListPage';

export default function SalesOrderPage() {
  return (
    <SimpleListPage
      title="Sales Orders"
      endpoint="/sales/sales-order"
      breadcrumbs={[{ label: 'Sales' }, { label: 'Sales Order' }]}
      columns={[
        { field: 'soNo', headerName: 'SO No' },
        { field: 'customer', headerName: 'Customer' },
        { field: 'status', headerName: 'Status' }
      ]}
    />
  );
}
