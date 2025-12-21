const documentStatusOptions = ['DRAFT', 'APPROVED', 'RELEASED', 'POSTED', 'CANCELLED'];
const qcStatusOptions = ['PENDING', 'HOLD', 'ACCEPTED', 'REJECTED'];
const debitNoteReasons = ['WEIGHT_DIFF', 'BAG_TYPE_DIFF', 'QUALITY_CLAIM', 'RATE_DIFF'];

export const moduleConfigs = {
  dashboard: {
    title: 'Dashboard',
    subtitle: 'Overview'
  },
  suppliers: {
    title: 'Suppliers',
    subtitle: 'Settings',
    createEndpoint: '/api/suppliers',
    listEndpoint: '/api/suppliers',
    fields: [
      { name: 'name', label: 'Supplier Name', type: 'text' },
      { name: 'code', label: 'Supplier Code', type: 'text' },
      { name: 'pan', label: 'PAN', type: 'text' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Name', field: 'name' },
      { label: 'Code', field: 'code' },
      { label: 'PAN', field: 'pan' }
    ]
  },
  items: {
    title: 'Items',
    subtitle: 'Settings',
    createEndpoint: '/api/items',
    listEndpoint: '/api/items',
    fields: [
      { name: 'name', label: 'Item Name', type: 'text' },
      { name: 'sku', label: 'SKU', type: 'text' },
      { name: 'uomId', label: 'UOM', type: 'select', optionsSource: 'uoms' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Name', field: 'name' },
      { label: 'SKU', field: 'sku' },
      { label: 'UOM', field: 'uomId' }
    ]
  },
  locations: {
    title: 'Locations (Godown/Storage)',
    subtitle: 'Settings',
    createEndpoint: '/api/locations',
    listEndpoint: '/api/locations',
    fields: [
      { name: 'name', label: 'Location Name', type: 'text' },
      { name: 'code', label: 'Location Code', type: 'text' },
      { name: 'locationType', label: 'Location Type', type: 'select', options: ['GODOWN', 'BIN'] }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Name', field: 'name' },
      { label: 'Code', field: 'code' },
      { label: 'Type', field: 'locationType' }
    ]
  },
  vehicles: {
    title: 'Vehicles',
    subtitle: 'Settings',
    createEndpoint: '/api/vehicles',
    listEndpoint: '/api/vehicles',
    fields: [{ name: 'vehicleNo', label: 'Vehicle Number', type: 'text' }],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Vehicle Number', field: 'vehicleNo' }
    ]
  },
  customers: {
    title: 'Customers',
    subtitle: 'Settings',
    createEndpoint: '/api/customers',
    listEndpoint: '/api/customers',
    fields: [
      { name: 'name', label: 'Customer Name', type: 'text' },
      { name: 'code', label: 'Customer Code', type: 'text' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Name', field: 'name' },
      { label: 'Code', field: 'code' }
    ]
  },
  brokers: {
    title: 'Brokers & Commission',
    subtitle: 'Settings',
    createEndpoint: '/api/brokers',
    listEndpoint: '/api/brokers',
    fields: [
      { name: 'name', label: 'Broker Name', type: 'text' },
      { name: 'code', label: 'Broker Code', type: 'text' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Name', field: 'name' },
      { label: 'Code', field: 'code' }
    ]
  },
  users: {
    title: 'Users & Roles',
    subtitle: 'Settings',
    createEndpoint: '/api/users',
    listEndpoint: '/api/users',
    fields: [
      { name: 'username', label: 'Username', type: 'text' },
      { name: 'fullName', label: 'Full Name', type: 'text' },
      { name: 'password', label: 'Password', type: 'password' },
      { name: 'roleName', label: 'Role', type: 'select', optionsSource: 'roles', optionValue: 'name', optionLabel: 'name' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Username', field: 'username' },
      { label: 'Full Name', field: 'fullName' },
      { label: 'Roles', field: 'roles' }
    ]
  },
  tdsRules: {
    title: 'TDS Rules',
    subtitle: 'Settings',
    createEndpoint: '/api/tds-rules',
    listEndpoint: '/api/tds-rules',
    fields: [
      { name: 'sectionCode', label: 'Section Code', type: 'text' },
      { name: 'ratePercent', label: 'Rate %', type: 'number' },
      { name: 'thresholdAmount', label: 'Threshold Amount', type: 'number' },
      { name: 'effectiveFrom', label: 'Effective From', type: 'date' },
      { name: 'effectiveTo', label: 'Effective To', type: 'date' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Section', field: 'sectionCode' },
      { label: 'Rate %', field: 'ratePercent' },
      { label: 'Threshold', field: 'thresholdAmount' }
    ]
  },
  rfq: {
    title: 'RFQ',
    subtitle: 'Purchase',
    createEndpoint: '/api/rfq',
    listEndpoint: '/api/rfq',
    createRoute: '/purchase/rfq/new',
    detailRouteBase: '/purchase/rfq',
    editRouteBase: '/purchase/rfq',
    useInlineCreate: false,
    enableFilters: true,
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'RFQ No', field: 'rfqNo' },
      { label: 'Supplier', field: 'supplierId' },
      { label: 'Status', field: 'status' },
      { label: 'Date', field: 'rfqDate' }
    ]
  },
  purchaseOrders: {
    title: 'Purchase Orders',
    subtitle: 'Purchase',
    createEndpoint: '/api/purchase-orders',
    listEndpoint: '/api/purchase-orders',
    createRoute: '/purchase/po/new',
    detailRouteBase: '/purchase/po',
    editRouteBase: '/purchase/po',
    useInlineCreate: false,
    enableFilters: true,
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'PO No', field: 'poNo' },
      { label: 'Supplier', field: 'supplierId' },
      { label: 'Status', field: 'status' },
      { label: 'Date', field: 'poDate' }
    ]
  },
  weighbridgeIn: {
    title: 'Weighbridge In',
    subtitle: 'Purchase',
    createEndpoint: '/api/weighbridge/tickets',
    listEndpoint: '/api/weighbridge/tickets',
    fields: [
      { name: 'ticketNo', label: 'Ticket Number', type: 'text' },
      { name: 'vehicleNo', label: 'Vehicle Number', type: 'text' },
      { name: 'supplierId', label: 'Supplier', type: 'select', optionsSource: 'suppliers' },
      { name: 'itemId', label: 'Item', type: 'select', optionsSource: 'items' },
      { name: 'dateIn', label: 'Date In', type: 'date' },
      { name: 'timeIn', label: 'Time In', type: 'time' },
      { name: 'grossWeight', label: 'Gross Weight', type: 'number' },
      { name: 'tareWeight', label: 'Tare Weight', type: 'number' }
    ],
    buildPayload: (values) => ({
      ticketNo: values.ticketNo,
      vehicleNo: values.vehicleNo,
      supplierId: Number(values.supplierId),
      itemId: Number(values.itemId),
      dateIn: values.dateIn,
      timeIn: values.timeIn,
      readings: [
        { readingType: 'GROSS', weight: Number(values.grossWeight), readingTime: new Date().toISOString() },
        { readingType: 'TARE', weight: Number(values.tareWeight), readingTime: new Date().toISOString() }
      ]
    }),
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Ticket No', field: 'ticketNo' },
      { label: 'Gross', field: 'gross' },
      { label: 'Tare', field: 'tare' },
      { label: 'Net', field: 'net' }
    ]
  },
  grn: {
    title: 'GRN',
    subtitle: 'Purchase',
    createEndpoint: '/api/grn',
    listEndpoint: '/api/grn',
    fields: [
      { name: 'grnNo', label: 'GRN Number', type: 'text' },
      { name: 'supplierId', label: 'Supplier', type: 'select', optionsSource: 'suppliers' },
      { name: 'weighbridgeTicketId', label: 'Weighbridge Ticket', type: 'select', optionsSource: 'tickets' },
      { name: 'grnDate', label: 'GRN Date', type: 'date' },
      { name: 'itemId', label: 'Item', type: 'select', optionsSource: 'items' },
      { name: 'bagType', label: 'Bag Type', type: 'text' },
      { name: 'bagCount', label: 'Bag Count', type: 'number' },
      { name: 'quantity', label: 'Quantity', type: 'number' },
      { name: 'weight', label: 'Weight', type: 'number' }
    ],
    buildPayload: (values) => ({
      grnNo: values.grnNo,
      supplierId: Number(values.supplierId),
      weighbridgeTicketId: Number(values.weighbridgeTicketId),
      grnDate: values.grnDate,
      lines: [
        {
          itemId: Number(values.itemId),
          bagType: values.bagType,
          bagCount: Number(values.bagCount),
          quantity: Number(values.quantity),
          weight: Number(values.weight)
        }
      ]
    }),
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'GRN No', field: 'grnNo' },
      { label: 'Status', field: 'status' }
    ]
  },
  qc: {
    title: 'QC Inspection',
    subtitle: 'Purchase',
    createEndpoint: '/api/qc/inspections',
    listEndpoint: '/api/qc/inspections',
    fields: [
      { name: 'grnLineId', label: 'GRN Line ID', type: 'number' },
      { name: 'status', label: 'QC Status', type: 'select', options: qcStatusOptions },
      { name: 'inspectionDate', label: 'Inspection Date', type: 'date' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Status', field: 'status' }
    ]
  },
  purchaseInvoice: {
    title: 'Purchase Invoice',
    subtitle: 'Purchase',
    createEndpoint: '/api/purchase-invoices',
    listEndpoint: '/api/purchase-invoices',
    fields: [
      { name: 'invoiceNo', label: 'Invoice Number', type: 'text' },
      { name: 'supplierId', label: 'Supplier', type: 'select', optionsSource: 'suppliers' },
      { name: 'invoiceDate', label: 'Invoice Date', type: 'date' },
      { name: 'itemId', label: 'Item', type: 'select', optionsSource: 'items' },
      { name: 'quantity', label: 'Quantity', type: 'number' },
      { name: 'lineAmount', label: 'Line Amount', type: 'number' }
    ],
    buildPayload: (values) => ({
      invoiceNo: values.invoiceNo,
      supplierId: Number(values.supplierId),
      invoiceDate: values.invoiceDate,
      lines: [
        {
          itemId: Number(values.itemId),
          quantity: Number(values.quantity),
          lineAmount: Number(values.lineAmount)
        }
      ]
    }),
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Invoice No', field: 'invoiceNo' },
      { label: 'Total', field: 'totalAmount' },
      { label: 'Net Payable', field: 'netPayable' }
    ]
  },
  debitNote: {
    title: 'Debit Note',
    subtitle: 'Purchase',
    createEndpoint: '/api/debit-notes',
    listEndpoint: '/api/debit-notes',
    fields: [
      { name: 'debitNoteNo', label: 'Debit Note Number', type: 'text' },
      { name: 'supplierId', label: 'Supplier', type: 'select', optionsSource: 'suppliers' },
      { name: 'reason', label: 'Reason', type: 'select', options: debitNoteReasons }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Debit Note No', field: 'debitNoteNo' },
      { label: 'Supplier', field: 'supplierId' },
      { label: 'Reason', field: 'reason' }
    ]
  },
  salesOrders: {
    title: 'Sales Orders',
    subtitle: 'Sales',
    createEndpoint: '/api/sales-orders',
    listEndpoint: '/api/sales-orders',
    fields: [
      { name: 'soNo', label: 'Sales Order Number', type: 'text' },
      { name: 'customerId', label: 'Customer', type: 'select', optionsSource: 'customers' },
      { name: 'status', label: 'Status', type: 'select', options: documentStatusOptions }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'SO No', field: 'soNo' },
      { label: 'Customer', field: 'customerId' },
      { label: 'Status', field: 'status' }
    ]
  },
  weighbridgeOut: {
    title: 'Weighbridge Out',
    subtitle: 'Sales',
    createEndpoint: '/api/weighbridge/tickets',
    listEndpoint: '/api/weighbridge/tickets',
    fields: [
      { name: 'ticketNo', label: 'Ticket Number', type: 'text' },
      { name: 'vehicleNo', label: 'Vehicle Number', type: 'text' },
      { name: 'supplierId', label: 'Supplier', type: 'select', optionsSource: 'suppliers' },
      { name: 'itemId', label: 'Item', type: 'select', optionsSource: 'items' },
      { name: 'dateIn', label: 'Date Out', type: 'date' },
      { name: 'timeIn', label: 'Time Out', type: 'time' },
      { name: 'grossWeight', label: 'Gross Weight', type: 'number' },
      { name: 'tareWeight', label: 'Tare Weight', type: 'number' }
    ],
    buildPayload: (values) => ({
      ticketNo: values.ticketNo,
      vehicleNo: values.vehicleNo,
      supplierId: Number(values.supplierId),
      itemId: Number(values.itemId),
      dateIn: values.dateIn,
      timeIn: values.timeIn,
      readings: [
        { readingType: 'GROSS', weight: Number(values.grossWeight), readingTime: new Date().toISOString() },
        { readingType: 'TARE', weight: Number(values.tareWeight), readingTime: new Date().toISOString() }
      ]
    }),
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Ticket No', field: 'ticketNo' },
      { label: 'Gross', field: 'gross' },
      { label: 'Tare', field: 'tare' },
      { label: 'Net', field: 'net' }
    ]
  },
  delivery: {
    title: 'Delivery',
    subtitle: 'Sales',
    createEndpoint: '/api/deliveries',
    listEndpoint: '/api/deliveries',
    fields: [
      { name: 'deliveryNo', label: 'Delivery Number', type: 'text' },
      { name: 'salesOrderId', label: 'Sales Order', type: 'select', optionsSource: 'salesOrders' }
    ],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Delivery No', field: 'deliveryNo' },
      { label: 'Sales Order', field: 'salesOrderId' }
    ]
  },
  salesInvoice: {
    title: 'Sales Invoice',
    subtitle: 'Sales',
    createEndpoint: '/api/sales-invoices',
    listEndpoint: '/api/sales-invoices',
    fields: [
      { name: 'invoiceNo', label: 'Invoice Number', type: 'text' },
      { name: 'customerId', label: 'Customer', type: 'select', optionsSource: 'customers' },
      { name: 'brokerId', label: 'Broker (optional)', type: 'select', optionsSource: 'brokers' },
      { name: 'invoiceDate', label: 'Invoice Date', type: 'date' },
      { name: 'totalAmount', label: 'Total Amount', type: 'number' }
    ],
    buildPayload: (values) => ({
      invoiceNo: values.invoiceNo,
      customerId: Number(values.customerId),
      brokerId: values.brokerId ? Number(values.brokerId) : null,
      invoiceDate: values.invoiceDate,
      totalAmount: Number(values.totalAmount)
    }),
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Invoice No', field: 'invoiceNo' },
      { label: 'Customer', field: 'customerId' },
      { label: 'Total', field: 'totalAmount' },
      { label: 'Status', field: 'status' }
    ]
  },
  stockOnHand: {
    title: 'Stock On Hand',
    subtitle: 'Inventory',
    listEndpoint: '/api/stock-ledger',
    fields: [],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Doc Type', field: 'docType' },
      { label: 'Item', field: 'itemId' },
      { label: 'Quantity', field: 'quantity' },
      { label: 'Status', field: 'status' }
    ]
  },
  stockLedger: {
    title: 'Stock Ledger',
    subtitle: 'Inventory',
    listEndpoint: '/api/stock-ledger',
    fields: [],
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Doc Type', field: 'docType' },
      { label: 'Txn Type', field: 'txnType' },
      { label: 'Quantity', field: 'quantity' },
      { label: 'Status', field: 'status' }
    ]
  },
  stockTransfer: {
    title: 'Stock Transfer',
    subtitle: 'Inventory',
    createEndpoint: '/api/stock-transfers',
    listEndpoint: '/api/stock-transfers',
    fields: [
      { name: 'itemId', label: 'Item', type: 'select', optionsSource: 'items' },
      { name: 'fromLocationId', label: 'From Location', type: 'select', optionsSource: 'locations' },
      { name: 'toLocationId', label: 'To Location', type: 'select', optionsSource: 'locations' },
      { name: 'quantity', label: 'Quantity', type: 'number' },
      { name: 'weight', label: 'Weight', type: 'number' }
    ],
    buildPayload: (values) => ({
      itemId: Number(values.itemId),
      fromLocationId: Number(values.fromLocationId),
      toLocationId: Number(values.toLocationId),
      quantity: Number(values.quantity),
      weight: Number(values.weight)
    }),
    columns: [
      { label: 'ID', field: 'id' },
      { label: 'Item', field: 'itemId' },
      { label: 'From', field: 'fromLocationId' },
      { label: 'To', field: 'toLocationId' },
      { label: 'Status', field: 'status' }
    ]
  },
  reports: {
    title: 'Reports',
    subtitle: 'Reports',
    fields: [],
    columns: []
  },
  settings: {
    title: 'Settings',
    subtitle: 'Settings',
    fields: [],
    columns: []
  }
};
