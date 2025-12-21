import SimpleListPage from '../common/SimpleListPage';

export default function ItemBalancesPage() {
  return (
    <SimpleListPage
      title="Item Balances"
      endpoint="/inventory/item-balances"
      breadcrumbs={[{ label: 'Inventory' }, { label: 'Item Balances' }]}
      columns={[
        { field: 'item', headerName: 'Item' },
        { field: 'onHand', headerName: 'On Hand' },
        { field: 'uom', headerName: 'UOM' }
      ]}
    />
  );
}
