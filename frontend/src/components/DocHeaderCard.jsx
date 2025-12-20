import React from 'react'
import { CCard, CCardBody, CRow, CCol } from '@coreui/react'

const DocHeaderCard = ({ title, subtitle, children }) => (
  <CCard className="mb-3">
    <CCardBody>
      <CRow>
        <CCol>
          <h4>{title}</h4>
          <p className="text-body-secondary">{subtitle}</p>
        </CCol>
      </CRow>
      {children}
    </CCardBody>
  </CCard>
)

export default DocHeaderCard
