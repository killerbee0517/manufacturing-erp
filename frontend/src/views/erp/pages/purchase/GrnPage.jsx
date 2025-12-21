import SimpleListPage from '../common/SimpleListPage';

export default function GrnPage() {
  return (
    <SimpleListPage
      title="GRN"
      endpoint="/purchase/grn"
      breadcrumbs={[{ label: 'Purchase' }, { label: 'GRN' }]}
      columns={[
        { field: 'grnNo', headerName: 'GRN No' },
        { field: 'supplier', headerName: 'Supplier' },
        { field: 'date', headerName: 'Date' }
      ]}
    />
  );
}
