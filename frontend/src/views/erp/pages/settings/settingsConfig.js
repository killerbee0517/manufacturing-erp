export const settingsEntities = {
  company: {
    title: 'Company',
    fields: [
      { name: 'name', label: 'Company Name', required: true },
      { name: 'gstin', label: 'GSTIN', required: true },
      { name: 'address', label: 'Registered Address', required: true }
    ],
    columns: [
      { field: 'name', headerName: 'Company Name' },
      { field: 'gstin', headerName: 'GSTIN' },
      { field: 'address', headerName: 'Address' }
    ]
  },
  ledgers: {
    title: 'Ledgers',
    fields: [
      { name: 'name', label: 'Ledger Name', required: true },
      { name: 'group', label: 'Ledger Group', required: true, type: 'select', endpoint: '/settings/ledger-groups' },
      { name: 'gstin', label: 'GSTIN' }
    ],
    columns: [
      { field: 'name', headerName: 'Ledger Name' },
      { field: 'group', headerName: 'Group' },
      { field: 'gstin', headerName: 'GSTIN' }
    ]
  },
  'ledger-groups': {
    title: 'Ledger Groups',
    fields: [
      { name: 'name', label: 'Group Name', required: true },
      { name: 'nature', label: 'Nature', required: true }
    ],
    columns: [
      { field: 'name', headerName: 'Group Name' },
      { field: 'nature', headerName: 'Nature' }
    ]
  },
  'stock-items': {
    title: 'Stock Items',
    fields: [
      { name: 'name', label: 'Item Name', required: true },
      { name: 'sku', label: 'SKU', required: true },
      { name: 'uom', label: 'UOM', required: true, type: 'select', endpoint: '/settings/uom' }
    ],
    columns: [
      { field: 'name', headerName: 'Item Name' },
      { field: 'sku', headerName: 'SKU' },
      { field: 'uom', headerName: 'UOM' }
    ]
  },
  'stock-groups': {
    title: 'Stock Groups',
    fields: [
      { name: 'name', label: 'Group Name', required: true },
      { name: 'category', label: 'Category', required: true }
    ],
    columns: [
      { field: 'name', headerName: 'Group Name' },
      { field: 'category', headerName: 'Category' }
    ]
  },
  uom: {
    title: 'Units of Measure',
    fields: [
      { name: 'name', label: 'Unit Name', required: true },
      { name: 'symbol', label: 'Symbol', required: true }
    ],
    columns: [
      { field: 'name', headerName: 'Unit Name' },
      { field: 'symbol', headerName: 'Symbol' }
    ]
  },
  godowns: {
    title: 'Godowns',
    fields: [
      { name: 'name', label: 'Godown Name', required: true },
      { name: 'location', label: 'Location', required: true }
    ],
    columns: [
      { field: 'name', headerName: 'Godown Name' },
      { field: 'location', headerName: 'Location' }
    ]
  },
  'cost-categories': {
    title: 'Cost Categories',
    fields: [{ name: 'name', label: 'Category Name', required: true }],
    columns: [{ field: 'name', headerName: 'Category Name' }]
  },
  'cost-centers': {
    title: 'Cost Centers',
    fields: [
      { name: 'name', label: 'Cost Center Name', required: true },
      { name: 'category', label: 'Cost Category', required: true, type: 'select', endpoint: '/settings/cost-categories' }
    ],
    columns: [
      { field: 'name', headerName: 'Cost Center Name' },
      { field: 'category', headerName: 'Category' }
    ]
  },
  'gst-hsn': {
    title: 'GST & HSN',
    fields: [
      { name: 'hsn', label: 'HSN Code', required: true },
      { name: 'description', label: 'Description', required: true },
      { name: 'taxRate', label: 'Tax Rate %', required: true, type: 'number' }
    ],
    columns: [
      { field: 'hsn', headerName: 'HSN Code' },
      { field: 'description', headerName: 'Description' },
      { field: 'taxRate', headerName: 'Tax Rate %' }
    ]
  },
  'document-series': {
    title: 'Document Series',
    fields: [
      { name: 'name', label: 'Series Name', required: true },
      { name: 'prefix', label: 'Prefix', required: true },
      { name: 'nextNumber', label: 'Next Number', required: true, type: 'number' }
    ],
    columns: [
      { field: 'name', headerName: 'Series Name' },
      { field: 'prefix', headerName: 'Prefix' },
      { field: 'nextNumber', headerName: 'Next Number' }
    ]
  }
};
