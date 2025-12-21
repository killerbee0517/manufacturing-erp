import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function SimpleListPage({ title, endpoint, columns, breadcrumbs }) {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(endpoint)
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [endpoint]);

  return (
    <MainCard>
      <PageHeader title={title} breadcrumbs={breadcrumbs} />
      <DataTable columns={columns} rows={rows} loading={loading} emptyMessage={`No ${title.toLowerCase()} found.`} />
    </MainCard>
  );
}

SimpleListPage.propTypes = {
  title: PropTypes.string.isRequired,
  endpoint: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      field: PropTypes.string.isRequired,
      headerName: PropTypes.string.isRequired
    })
  ).isRequired,
  breadcrumbs: PropTypes.array
};

SimpleListPage.defaultProps = {
  breadcrumbs: []
};
