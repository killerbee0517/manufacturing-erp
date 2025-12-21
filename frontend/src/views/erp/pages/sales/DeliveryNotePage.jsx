import SimpleListPage from '../common/SimpleListPage';

export default function DeliveryNotePage() {
  return (
    <SimpleListPage
      title="Delivery Note"
      endpoint="/sales/delivery-note"
      breadcrumbs={[{ label: 'Sales' }, { label: 'Delivery Note' }]}
      columns={[
        { field: 'deliveryNo', headerName: 'Delivery No' },
        { field: 'customer', headerName: 'Customer' },
        { field: 'date', headerName: 'Date' }
      ]}
    />
  );
}
