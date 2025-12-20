import React from 'react'
import { CTable, CTableBody, CTableHead, CTableRow, CTableHeaderCell, CTableDataCell } from '@coreui/react'

const ChargesGrid = ({ charges }) => (
  <CTable striped>
    <CTableHead>
      <CTableRow>
        <CTableHeaderCell>Charge</CTableHeaderCell>
        <CTableHeaderCell>Amount</CTableHeaderCell>
      </CTableRow>
    </CTableHead>
    <CTableBody>
      {charges?.map((charge, index) => (
        <CTableRow key={index}>
          <CTableDataCell>{charge.name}</CTableDataCell>
          <CTableDataCell>{charge.amount}</CTableDataCell>
        </CTableRow>
      ))}
    </CTableBody>
  </CTable>
)

export default ChargesGrid
