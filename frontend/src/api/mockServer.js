import axios from 'axios';
import apiClient from './client';

const defaultAdapter = axios.defaults.adapter;

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const createId = (() => {
  let current = 1000;
  return () => {
    current += 1;
    return current;
  };
})();

const store = {
  settings: {
    company: [],
    ledgers: [],
    'ledger-groups': [],
    'stock-items': [],
    'stock-groups': [],
    uom: [],
    godowns: [],
    'cost-categories': [],
    'cost-centers': [],
    'gst-hsn': [],
    'document-series': []
  },
  vouchers: [],
  production: {
    templates: [],
    orders: [],
    executions: [],
    reprocess: []
  }
};

const sampleTemplates = [
  {
    id: 1,
    name: 'Appam Batter',
    steps: [
      { id: 1, name: 'Soak rice', description: 'Soak rice for 6 hours.' },
      { id: 2, name: 'Grind', description: 'Grind with coconut to smooth paste.' },
      { id: 3, name: 'Ferment', description: 'Ferment overnight.' }
    ]
  },
  {
    id: 2,
    name: 'Chilli Powder',
    steps: [
      { id: 1, name: 'Drying', description: 'Dry chillies to 8% moisture.' },
      { id: 2, name: 'Grinding', description: 'Grind into fine powder.' }
    ]
  }
];

const seedSettings = () => {
  store.settings.company = [
    { id: 1, name: 'Kaveri Foods', gstin: '33AAAPL1234C1ZQ', address: 'Coimbatore' }
  ];
  store.settings['ledger-groups'] = [
    { id: 11, name: 'Sundry Debtors', nature: 'Assets' },
    { id: 12, name: 'Sundry Creditors', nature: 'Liabilities' }
  ];
  store.settings.ledgers = [
    { id: 21, name: 'Sri Vinayaga Traders', group: 'Sundry Creditors' },
    { id: 22, name: 'Nalanda Supermarket', group: 'Sundry Debtors' }
  ];
  store.settings['stock-groups'] = [
    { id: 31, name: 'Spices', category: 'Finished Goods' }
  ];
  store.settings['stock-items'] = [
    { id: 41, name: 'Chilli Powder 500g', sku: 'CP-500', uom: 'Pack' }
  ];
  store.settings.uom = [
    { id: 51, name: 'Kg', symbol: 'kg' },
    { id: 52, name: 'Pack', symbol: 'pk' }
  ];
  store.settings.godowns = [
    { id: 61, name: 'Main Warehouse', location: 'Coimbatore' }
  ];
  store.settings['cost-categories'] = [
    { id: 71, name: 'Manufacturing' }
  ];
  store.settings['cost-centers'] = [
    { id: 81, name: 'Unit A', category: 'Manufacturing' }
  ];
  store.settings['gst-hsn'] = [
    { id: 91, hsn: '0904', description: 'Chillies, crushed or ground', taxRate: 5 }
  ];
  store.settings['document-series'] = [
    { id: 101, name: 'FY25 Invoice', prefix: 'INV/25', nextNumber: 1201 }
  ];
};

const seedDemoTransactions = () => {
  store.vouchers = [
    {
      id: 201,
      type: 'purchase-invoice',
      voucherNo: 'PI-248',
      date: '2024-11-20',
      party: 'Sri Vinayaga Traders',
      total: 15600,
      lines: [
        { id: 1, item: 'Chilli Powder 500g', qty: 100, rate: 120, amount: 12000, tax: 5 }
      ]
    }
  ];
};

seedSettings();
store.production.templates = [...sampleTemplates];

const buildResponse = (config, data, status = 200) => ({
  data,
  status,
  statusText: status === 200 ? 'OK' : 'CREATED',
  headers: {},
  config
});

const parsePayload = (config) => {
  if (!config?.data) return {};
  if (typeof config.data === 'string') {
    try {
      return JSON.parse(config.data);
    } catch (error) {
      return {};
    }
  }
  return config.data;
};

const handleSettings = (config, parts) => {
  const entity = parts[1];
  const id = parts[2] ? Number(parts[2]) : null;
  if (!store.settings[entity]) {
    return buildResponse(config, []);
  }

  if (config.method === 'get') {
    if (id) {
      return buildResponse(config, store.settings[entity].find((item) => item.id === id) || null);
    }
    return buildResponse(config, store.settings[entity]);
  }

  const payload = parsePayload(config);
  if (config.method === 'post') {
    const record = { id: createId(), ...payload };
    store.settings[entity].push(record);
    return buildResponse(config, record, 201);
  }

  if (config.method === 'put') {
    const index = store.settings[entity].findIndex((item) => item.id === id);
    if (index >= 0) {
      store.settings[entity][index] = { ...store.settings[entity][index], ...payload };
      return buildResponse(config, store.settings[entity][index]);
    }
    return buildResponse(config, null, 404);
  }

  if (config.method === 'delete') {
    store.settings[entity] = store.settings[entity].filter((item) => item.id !== id);
    return buildResponse(config, { success: true });
  }

  return buildResponse(config, []);
};

const handleVouchers = (config, parts) => {
  const id = parts[1] ? Number(parts[1]) : null;
  if (config.method === 'get') {
    if (id) {
      return buildResponse(config, store.vouchers.find((item) => item.id === id) || null);
    }
    const type = config.params?.type;
    const filtered = type ? store.vouchers.filter((item) => item.type === type) : store.vouchers;
    return buildResponse(config, filtered);
  }

  if (config.method === 'post') {
    const payload = parsePayload(config);
    const record = {
      id: createId(),
      ...payload
    };
    store.vouchers.push(record);
    return buildResponse(config, record, 201);
  }

  return buildResponse(config, []);
};

const handleReports = (config, parts) => {
  const reportType = parts[1];
  if (reportType === 'ledger') {
    return buildResponse(config, [
      {
        id: 1,
        date: '2024-11-20',
        particulars: 'Purchase Invoice',
        voucherType: 'Purchase',
        voucherNo: 'PI-248',
        debit: 15600,
        credit: 0,
        balance: 15600
      },
      {
        id: 2,
        date: '2024-11-22',
        particulars: 'Payment Receipt',
        voucherType: 'Receipt',
        voucherNo: 'RC-018',
        debit: 0,
        credit: 5600,
        balance: 10000
      }
    ]);
  }

  if (reportType === 'outstanding') {
    return buildResponse(config, [
      { id: 1, party: 'Nalanda Supermarket', pendingAmount: 42000, dueDate: '2024-12-15', overdueDays: 8 },
      { id: 2, party: 'Sri Vinayaga Traders', pendingAmount: 15600, dueDate: '2024-12-01', overdueDays: 22 }
    ]);
  }

  if (reportType === 'ageing') {
    return buildResponse(config, [
      { id: 1, bucket: '<30', amount: 12000 },
      { id: 2, bucket: '30-60', amount: 18000 },
      { id: 3, bucket: '60-90', amount: 8000 },
      { id: 4, bucket: '90-120', amount: 4500 },
      { id: 5, bucket: '120-365', amount: 2200 },
      { id: 6, bucket: '>365', amount: 900 }
    ]);
  }

  return buildResponse(config, []);
};

const handleProduction = (config, parts) => {
  const resource = parts[1];
  if (resource === 'templates') {
    if (config.method === 'get') {
      return buildResponse(config, store.production.templates);
    }
    if (config.method === 'post') {
      const payload = parsePayload(config);
      const record = { id: createId(), ...payload };
      store.production.templates.push(record);
      return buildResponse(config, record, 201);
    }
  }

  if (resource === 'orders') {
    if (config.method === 'get') {
      return buildResponse(config, store.production.orders);
    }
    if (config.method === 'post') {
      const payload = parsePayload(config);
      const record = { id: createId(), status: 'Planned', ...payload };
      store.production.orders.push(record);
      return buildResponse(config, record, 201);
    }
  }

  if (resource === 'executions' && config.method === 'post') {
    const payload = parsePayload(config);
    const record = { id: createId(), ...payload };
    store.production.executions.push(record);
    return buildResponse(config, record, 201);
  }

  if (resource === 'reprocess-scrap') {
    if (config.method === 'get') {
      return buildResponse(config, store.production.reprocess);
    }
    if (config.method === 'post') {
      const payload = parsePayload(config);
      const record = { id: createId(), ...payload };
      store.production.reprocess.push(record);
      return buildResponse(config, record, 201);
    }
  }

  return buildResponse(config, []);
};

const handleDevTools = (config, parts) => {
  const action = parts[2];
  if (config.method === 'post' && action === 'settings') {
    seedSettings();
    return buildResponse(config, { success: true });
  }
  if (config.method === 'post' && action === 'demo-transactions') {
    seedDemoTransactions();
    return buildResponse(config, { success: true });
  }
  return buildResponse(config, { success: true });
};

const handleGenericList = (config, path) => {
  if (config.method === 'get') {
    return buildResponse(config, []);
  }
  if (config.method === 'post') {
    return buildResponse(config, { success: true }, 201);
  }
  return buildResponse(config, { success: true });
};

export const enableMockServer = () => {
  axios.defaults.adapter = async (config) => {
    if (!config.url) {
      return defaultAdapter(config);
    }

    const url = config.url.startsWith('/api') ? config.url.slice(4) : config.url;
    const path = url.startsWith('/') ? url : `/${url}`;
    const parts = path.split('/').filter(Boolean);

    await delay(150);

    if (parts[0] === 'settings') {
      return handleSettings(config, parts);
    }

    if (parts[0] === 'vouchers') {
      return handleVouchers(config, parts);
    }

    if (parts[0] === 'reports') {
      return handleReports(config, parts);
    }

    if (parts[0] === 'production') {
      return handleProduction(config, parts);
    }

    if (parts[0] === 'dev' && parts[1] === 'seed') {
      return handleDevTools(config, parts);
    }

    if (['purchase', 'sales', 'inventory'].includes(parts[0])) {
      return handleGenericList(config, path);
    }

    return handleGenericList(config, path);
  };
  apiClient.defaults.adapter = axios.defaults.adapter;
};
