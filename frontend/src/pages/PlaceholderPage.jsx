import React from 'react'
import {
  CBadge,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormInput,
  CFormSelect,
  CRow,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow
} from '@coreui/react'

const PlaceholderPage = ({ title, subtitle, description, formFields, tableColumns }) => (
  <div>
    <CCard className="mb-4">
      <CCardHeader className="d-flex align-items-center justify-content-between">
        <div>
          <div className="text-medium-emphasis text-uppercase small">{subtitle}</div>
          <div className="h5 mb-0">{title}</div>
        </div>
        <CBadge color="info" className="ms-2">Stub</CBadge>
      </CCardHeader>
      <CCardBody>
        <p className="text-medium-emphasis mb-0">{description}</p>
      </CCardBody>
    </CCard>

    <CRow className="g-4">
      <CCol lg={5}>
        <CCard>
          <CCardHeader>Quick Entry</CCardHeader>
          <CCardBody>
            <CForm className="row g-3">
              {formFields.map((field) => (
                <CCol xs={12} key={field.label}>
                  {field.type === 'select' ? (
                    <CFormSelect label={field.label} options={field.options} />
                  ) : (
                    <CFormInput label={field.label} placeholder={field.placeholder} />
                  )}
                </CCol>
              ))}
            </CForm>
          </CCardBody>
        </CCard>
      </CCol>
      <CCol lg={7}>
        <CCard>
          <CCardHeader>Recent Activity</CCardHeader>
          <CCardBody>
            <CTable striped hover responsive>
              <CTableHead>
                <CTableRow>
                  {tableColumns.map((column) => (
                    <CTableHeaderCell key={column}>{column}</CTableHeaderCell>
                  ))}
                </CTableRow>
              </CTableHead>
              <CTableBody>
                <CTableRow>
                  {tableColumns.map((column, index) => (
                    <CTableDataCell key={column}>
                      {index === tableColumns.length - 1 ? (
                        <CBadge color="warning">Pending</CBadge>
                      ) : (
                        `Sample ${column}`
                      )}
                    </CTableDataCell>
                  ))}
                </CTableRow>
                <CTableRow>
                  {tableColumns.map((column, index) => (
                    <CTableDataCell key={column}>
                      {index === tableColumns.length - 1 ? (
                        <CBadge color="success">Approved</CBadge>
                      ) : (
                        `Sample ${column}`
                      )}
                    </CTableDataCell>
                  ))}
                </CTableRow>
              </CTableBody>
            </CTable>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  </div>
)

PlaceholderPage.defaultProps = {
  subtitle: 'Module',
  description: 'This section is wired to the CoreUI layout. Replace these placeholders with real data.',
  formFields: [],
  tableColumns: []
}

export default PlaceholderPage
