import React from 'react'
import { CButton, CModal, CModalBody, CModalFooter, CModalHeader } from '@coreui/react'

const ConfirmDialog = ({ visible, title, message, onCancel, onConfirm }) => (
  <CModal visible={visible} onClose={onCancel}>
    <CModalHeader>{title}</CModalHeader>
    <CModalBody>{message}</CModalBody>
    <CModalFooter>
      <CButton color="secondary" onClick={onCancel}>Cancel</CButton>
      <CButton color="primary" onClick={onConfirm}>Confirm</CButton>
    </CModalFooter>
  </CModal>
)

export default ConfirmDialog
