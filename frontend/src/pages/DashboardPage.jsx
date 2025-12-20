import React from 'react'
import { CCard, CCardBody, CCardHeader, CRow, CCol } from '@coreui/react'

const DashboardPage = () => (
  <div>
    <CCard className="mb-4">
      <CCardHeader>Dashboard</CCardHeader>
      <CCardBody>
        Welcome to the Manufacturing ERP dashboard. Use the sidebar to navigate modules.
      </CCardBody>
    </CCard>
    <CRow>
      <CCol md={4}>
        <CCard>
          <CCardHeader>Purchase</CCardHeader>
          <CCardBody>Track GRN, QC, and Purchase Invoices.</CCardBody>
        </CCard>
      </CCol>
      <CCol md={4}>
        <CCard>
          <CCardHeader>Sales</CCardHeader>
          <CCardBody>Manage Sales Orders and Invoices.</CCardBody>
        </CCard>
      </CCol>
      <CCol md={4}>
        <CCard>
          <CCardHeader>Inventory</CCardHeader>
          <CCardBody>Stock ledger and transfers at a glance.</CCardBody>
        </CCard>
      </CCol>
    </CRow>
  </div>
)

export default DashboardPage
