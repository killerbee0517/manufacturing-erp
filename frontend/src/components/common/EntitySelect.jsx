import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';

import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';

export default function EntitySelect({ label, endpoint, value, onChange, getOptionLabel, required }) {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!endpoint || !open) return;
    const handle = setTimeout(() => {
      setLoading(true);
      apiClient
        .get(endpoint, {
          params: {
            q: inputValue || undefined,
            limit: 20
          }
        })
        .then((response) => {
          const payload = response.data || [];
          setOptions(payload.content || payload);
        })
        .catch(() => {
          setOptions([]);
        })
        .finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(handle);
  }, [endpoint, inputValue, open]);

  useEffect(() => {
    if (!endpoint || !value?.id) return;
    if (options.some((option) => option?.id === value.id)) return;
    apiClient
      .get(`${endpoint}/${value.id}`)
      .then((response) => {
        if (!response.data) return;
        setOptions((prev) => {
          if (prev.some((option) => option?.id === response.data.id)) return prev;
          return [...prev, response.data];
        });
      })
      .catch(() => null);
  }, [endpoint, options, value]);

  return (
    <Autocomplete
      options={options}
      loading={loading}
      value={value}
      open={open}
      onOpen={() => setOpen(true)}
      onClose={() => setOpen(false)}
      onChange={(_, newValue) => onChange(newValue)}
      getOptionLabel={(option) => (getOptionLabel ? getOptionLabel(option) : option?.name || option?.label || option?.title || '')}
      inputValue={inputValue}
      onInputChange={(_, newInputValue) => setInputValue(newInputValue)}
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
