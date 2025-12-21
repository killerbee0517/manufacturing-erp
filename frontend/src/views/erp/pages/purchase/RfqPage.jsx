import SimpleListPage from '../common/SimpleListPage';

export default function RfqPage() {
  return (
    <SimpleListPage
      title="RFQ"
      endpoint="/purchase/rfq"
      breadcrumbs={[{ label: 'Purchase' }, { label: 'RFQ' }]}
      columns={[
        { field: 'rfqNo', headerName: 'RFQ No' },
        { field: 'status', headerName: 'Status' }
      ]}
    />
  );
}
