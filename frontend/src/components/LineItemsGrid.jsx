import React from 'react'
import { CTable, CTableBody, CTableHead, CTableRow, CTableHeaderCell, CTableDataCell } from '@coreui/react'

const LineItemsGrid = ({ items }) => (
  <CTable striped>
    <CTableHead>
      <CTableRow>
        <CTableHeaderCell>Item</CTableHeaderCell>
        <CTableHeaderCell>Qty</CTableHeaderCell>
        <CTableHeaderCell>Amount</CTableHeaderCell>
      </CTableRow>
    </CTableHead>
    <CTableBody>
      {items?.map((item, index) => (
        <CTableRow key={index}>
          <CTableDataCell>{item.name}</CTableDataCell>
          <CTableDataCell>{item.qty}</CTableDataCell>
          <CTableDataCell>{item.amount}</CTableDataCell>
        </CTableRow>
      ))}
    </CTableBody>
  </CTable>
)

export default LineItemsGrid
