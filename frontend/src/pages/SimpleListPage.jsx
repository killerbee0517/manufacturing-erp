import React from 'react'
import { useParams } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CTable, CTableBody, CTableHead, CTableRow, CTableHeaderCell, CTableDataCell } from '@coreui/react'

const SimpleListPage = ({ title }) => {
  const { section } = useParams()
  return (
    <CCard>
      <CCardHeader>{title} - {section}</CCardHeader>
      <CCardBody>
        <CTable striped>
          <CTableHead>
            <CTableRow>
              <CTableHeaderCell>#</CTableHeaderCell>
              <CTableHeaderCell>Name</CTableHeaderCell>
              <CTableHeaderCell>Status</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            <CTableRow>
              <CTableDataCell>1</CTableDataCell>
              <CTableDataCell>Sample record</CTableDataCell>
              <CTableDataCell>Draft</CTableDataCell>
            </CTableRow>
          </CTableBody>
        </CTable>
      </CCardBody>
    </CCard>
  )
}

export default SimpleListPage
