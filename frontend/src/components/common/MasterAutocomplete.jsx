import PropTypes from 'prop-types';
import { useCallback, useEffect, useMemo, useState } from 'react';

import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';

const defaultOptionLabel = (option) =>
  option?.name || option?.label || option?.title || option?.code || option?.vehicleNo || option?.sku || '';

export default function MasterAutocomplete({
  label,
  endpoint,
  value,
  onChange,
  optionLabelKey,
  optionValueKey = 'id',
  required = false,
  placeholder = '',
  size = 'medium',
  fullWidth = true,
  disabled = false,
  limit = 20,
  queryParams,
  freeSolo = false
}) {
  const [options, setOptions] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const getOptionValue = useCallback((option) => option?.[optionValueKey], [optionValueKey]);
  const getOptionLabel = useCallback(
    (option) => (optionLabelKey ? option?.[optionLabelKey] ?? '' : defaultOptionLabel(option)),
    [optionLabelKey]
  );

  const normalizeValue = useCallback((val) => (val === null || val === undefined ? '' : String(val)), []);

  const selectedOption = useMemo(() => {
    if (value === null || value === undefined || value === '') return null;
    const normalized = normalizeValue(value);
    return options.find((option) => normalizeValue(getOptionValue(option)) === normalized) || null;
  }, [options, value, getOptionValue, normalizeValue]);

  useEffect(() => {
    if (open) return;
    if (!selectedOption) {
      if (inputValue !== '') {
        setInputValue('');
      }
      return;
    }
    const label = getOptionLabel(selectedOption);
    if (label !== inputValue) {
      setInputValue(label);
    }
  }, [selectedOption, getOptionLabel, inputValue, open]);

  const fetchOptions = useCallback(
    async (searchValue) => {
      if (!endpoint) return;
      setLoading(true);
      try {
        const response = await apiClient.get(endpoint, {
          params: {
            q: searchValue || undefined,
            limit,
            ...(queryParams || {})
          }
        });
        const payload = response.data || [];
        setOptions(payload.content || payload);
      } catch {
        setOptions([]);
      } finally {
        setLoading(false);
      }
    },
    [endpoint, limit]
  );

  useEffect(() => {
    if (!open) return;
    const handle = setTimeout(() => {
      fetchOptions(inputValue);
    }, 300);
    return () => clearTimeout(handle);
  }, [inputValue, fetchOptions, open]);

  useEffect(() => {
    if (!endpoint || !open || options.length > 0 || inputValue) return;
    fetchOptions('');
  }, [endpoint, fetchOptions, inputValue, open, options.length]);

  useEffect(() => {
    if (!endpoint || optionValueKey !== 'id' || value === null || value === undefined || value === '') return;
    if (options.some((option) => option?.id === value)) return;
    apiClient
      .get(`${endpoint}/${value}`)
      .then((response) => {
        if (!response.data) return;
        setOptions((prev) => {
          if (prev.some((option) => option?.id === response.data.id)) return prev;
          return [...prev, response.data];
        });
      })
      .catch(() => null);
  }, [endpoint, optionValueKey, options, value]);

  return (
    <Autocomplete
      open={open}
      onOpen={() => setOpen(true)}
      onClose={() => setOpen(false)}
      options={options}
      loading={loading}
      value={selectedOption}
      onChange={(_, newValue) => onChange(newValue ? getOptionValue(newValue) : '')}
      inputValue={inputValue}
      onInputChange={(_, newInputValue) => setInputValue(newInputValue)}
      getOptionLabel={getOptionLabel}
      isOptionEqualToValue={(option, selected) => normalizeValue(getOptionValue(option)) === normalizeValue(getOptionValue(selected))}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          placeholder={placeholder}
          required={required}
        />
      )}
      fullWidth={fullWidth}
      size={size}
      disabled={disabled}
      freeSolo={freeSolo}
    />
  );
}

MasterAutocomplete.propTypes = {
  label: PropTypes.string.isRequired,
  endpoint: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  onChange: PropTypes.func.isRequired,
  optionLabelKey: PropTypes.string,
  optionValueKey: PropTypes.string,
  required: PropTypes.bool,
  placeholder: PropTypes.string,
  size: PropTypes.string,
  fullWidth: PropTypes.bool,
  disabled: PropTypes.bool,
  limit: PropTypes.number,
  queryParams: PropTypes.object,
  freeSolo: PropTypes.bool
};

MasterAutocomplete.defaultProps = {
  value: '',
  optionLabelKey: undefined,
  queryParams: undefined
};
