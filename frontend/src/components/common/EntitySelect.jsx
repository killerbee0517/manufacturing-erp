import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';

import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';

export default function EntitySelect({ label, endpoint, value, onChange, getOptionLabel, required }) {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!endpoint) return;
    setLoading(true);
    apiClient
      .get(endpoint)
      .then((response) => {
        setOptions(response.data || []);
      })
      .catch(() => {
        setOptions([]);
      })
      .finally(() => setLoading(false));
  }, [endpoint]);

  return (
    <Autocomplete
      options={options}
      loading={loading}
      value={value}
      onChange={(_, newValue) => onChange(newValue)}
      getOptionLabel={(option) => (getOptionLabel ? getOptionLabel(option) : option?.name || option?.label || option?.title || '')}
      renderInput={(params) => <TextField {...params} label={label} required={required} />}
      isOptionEqualToValue={(option, selected) => option?.id === selected?.id}
    />
  );
}

EntitySelect.propTypes = {
  label: PropTypes.string.isRequired,
  endpoint: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.object, PropTypes.string, PropTypes.number]),
  onChange: PropTypes.func.isRequired,
  getOptionLabel: PropTypes.func,
  required: PropTypes.bool
};

EntitySelect.defaultProps = {
  value: null,
  getOptionLabel: undefined,
  required: false
};
