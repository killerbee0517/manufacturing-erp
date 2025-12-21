import PropTypes from 'prop-types';
import { useMemo, useState } from 'react';

import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import { IconEdit, IconSearch, IconTrash } from '@tabler/icons-react';

export default function DataTable({
  columns,
  rows,
  loading,
  emptyMessage,
  onEdit,
  onDelete,
  onRowClick,
  searchPlaceholder
}) {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [search, setSearch] = useState('');

  const filteredRows = useMemo(() => {
    if (!search) return rows;
    const lowered = search.toLowerCase();
    return rows.filter((row) =>
      columns.some((column) => {
        const value = row[column.field];
        if (value === null || value === undefined) return false;
        if (typeof value === 'object') {
          return String(value.name || value.label || value.title || '').toLowerCase().includes(lowered);
        }
        return String(value).toLowerCase().includes(lowered);
      })
    );
  }, [rows, columns, search]);

  const pagedRows = useMemo(
    () => filteredRows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage),
    [filteredRows, page, rowsPerPage]
  );

  const hasActions = Boolean(onEdit || onDelete);

  return (
    <Stack spacing={2}>
      <TextField
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        placeholder={searchPlaceholder}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <IconSearch size={18} />
            </InputAdornment>
          )
        }}
      />
      {loading && <LinearProgress />}
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell key={column.field}>{column.headerName}</TableCell>
              ))}
              {hasActions && <TableCell align="right">Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {pagedRows.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={columns.length + (hasActions ? 1 : 0)}>
                  <Typography variant="body2" color="text.secondary">
                    {emptyMessage}
                  </Typography>
                </TableCell>
              </TableRow>
            )}
            {pagedRows.map((row) => (
              <TableRow
                key={row.id || row.key || JSON.stringify(row)}
                hover={Boolean(onRowClick)}
                sx={{ cursor: onRowClick ? 'pointer' : 'default' }}
                onClick={() => onRowClick?.(row)}
              >
                {columns.map((column) => (
                  <TableCell key={`${row.id}-${column.field}`}>
                    {column.render
                      ? column.render(row)
                      : (() => {
                          const value = row[column.field];
                          if (value && typeof value === 'object') {
                            return value.name || value.label || value.title || '-';
                          }
                          return value ?? '-';
                        })()}
                  </TableCell>
                ))}
                {hasActions && (
                  <TableCell align="right">
                    {onEdit && (
                      <IconButton size="small" onClick={(event) => {
                        event.stopPropagation();
                        onEdit(row);
                      }}>
                        <IconEdit size={18} />
                      </IconButton>
                    )}
                    {onDelete && (
                      <IconButton size="small" color="error" onClick={(event) => {
                        event.stopPropagation();
                        onDelete(row);
                      }}>
                        <IconTrash size={18} />
                      </IconButton>
                    )}
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <Box>
        <TablePagination
          component="div"
          count={filteredRows.length}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(event) => {
            setRowsPerPage(parseInt(event.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[5, 10, 25]}
        />
      </Box>
    </Stack>
  );
}

DataTable.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      field: PropTypes.string.isRequired,
      headerName: PropTypes.string.isRequired,
      render: PropTypes.func
    })
  ).isRequired,
  rows: PropTypes.arrayOf(PropTypes.object).isRequired,
  loading: PropTypes.bool,
  emptyMessage: PropTypes.string,
  onEdit: PropTypes.func,
  onDelete: PropTypes.func,
  onRowClick: PropTypes.func,
  searchPlaceholder: PropTypes.string
};

DataTable.defaultProps = {
  loading: false,
  emptyMessage: 'No records found',
  onEdit: undefined,
  onDelete: undefined,
  onRowClick: undefined,
  searchPlaceholder: 'Search records'
};
